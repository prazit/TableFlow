package com.tflow.trcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaTopics;
import com.tflow.model.data.DataManager;
import com.tflow.system.Environment;
import com.tflow.util.SerializeUtil;
import com.tflow.wcmd.TWcmd;
import com.tflow.zookeeper.ZKConfigNode;
import com.tflow.zookeeper.ZKConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * TODO: need to remove Client-Data-File-Checker after complete the Heartbeat function
 * TODO: Project Page Command to create new project: when request projectId < 0 (TRcmd send message to TWcmd)
 **/
public class TRcmd {

    private Logger log = LoggerFactory.getLogger(TRcmd.class);

    private boolean polling;

    private Environment environment;
    private EnvironmentConfigs environmentConfigs;

    public TRcmd() {
        /*nothing*/
    }

    @SuppressWarnings("unchecked")
    public void start() {

        ZKConfiguration zkConfiguration = null;
        try {
            zkConfiguration = createZK();
        } catch (Exception ex) {
            log.error("Zookeeper is required to run TRcmd, ", ex);
            System.exit(-1);
        }

        DataManager dataManager = new DataManager(environment, getClass().getSimpleName(), zkConfiguration);
        KafkaConsumer<String, byte[]> consumer = createConsumer();
        KafkaProducer<String, Object> dataProducer = createProducer();

        /*TODO: need to load readTopic from configuration*/
        String readTopic = KafkaTopics.PROJECT_READ.getTopic();
        String dataTopic = KafkaTopics.PROJECT_DATA.getTopic();
        consumer.subscribe(Collections.singletonList(readTopic));
        log.info("Subscribed to readTopic " + readTopic);

        Deserializer deserializer = null;
        try {
            deserializer = SerializeUtil.getDeserializer(environmentConfigs.getKafkaDeserializer());
        } catch (Exception ex) {
            log.error("Deserializer creation error: ", ex);
            return;
        }

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            for (ConsumerRecord<String, byte[]> record : records) {
                long offset = record.offset();
                String key = record.key();
                Object value;

                try {
                    value = deserializer.deserialize("", record.value());
                } catch (Exception ex) {
                    log.warn("Skip invalid message={}", new String(record.value(), StandardCharsets.ISO_8859_1));
                    log.warn("Deserialize error: ", ex);
                    continue;
                }

                /*TODO: add command to UpdateProjectCommandQueue*/
                ReadProjectCommand readProjectCommand = new ReadProjectCommand(offset, key, value, environmentConfigs, dataProducer, dataTopic, dataManager);
                log.info("Incoming message: {}", readProjectCommand.toString());

                /*test only*/
                /*TODO: move this execute block into UpdateProjectCommandQueue*/
                try {
                    readProjectCommand.execute();
                    log.info("Incoming message completed: {}", readProjectCommand.toString());
                } catch (InvalidParameterException inex) {
                    /*TODO: how to handle rejected command*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.warn("Message rejected: {}", readProjectCommand.toString());
                } catch (Exception ex) {
                    log.error("Hard error: ", ex);
                    log.warn("Message rejected: {}", readProjectCommand.toString());
                }

                /*Notice: sleeping commit-thread need to notify immediately*/
                boolean showEndLog = false;
                Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
                for (Thread thread : threadMap.keySet()) {
                    if (thread.getName().contains("Commit")) {
                        showEndLog = true;
                        StackTraceElement[] stackTraceElements = threadMap.get(thread);
                        log.warn("Notify Thread('{}') at {}", thread, stackTraceElements);
                        try {
                            thread.notify();
                        } catch (IllegalMonitorStateException ex) {
                            log.error("", ex);
                        }
                        break;
                    }
                }
                if (showEndLog) log.warn("Next roll is begin after notify commit-thread.");

            }
        }

        consumer.close();
    }

    private ZKConfiguration createZK() throws IOException, InterruptedException, KeeperException {
        ZKConfiguration zkConfiguration = new ZKConfiguration();
        zkConfiguration.connect();
        zkConfiguration.initial();

        environment = Environment.valueOf(zkConfiguration.getString(ZKConfigNode.ENVIRONMENT));
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());

        return zkConfiguration;
    }

    private KafkaConsumer<String, byte[]> createConsumer() {
        /*TODO: need to load consumer configuration*/
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "trcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer<>(props);
    }

    private KafkaProducer<String, Object> createProducer() {
        /*TODO: need to load producer configuration*/
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.encoding", StandardCharsets.UTF_8.name());
        props.put("value.serializer", environmentConfigs.getKafkaSerializer());
        return new KafkaProducer<>(props);
    }

    public void stop() {
        polling = false;
    }

}
