package com.tflow.tbcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaTopics;
import com.tflow.model.data.ProjectDataManager;
import com.tflow.system.Environment;
import com.tflow.util.SerializeUtil;
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

public class TBcmd {

    private Logger log = LoggerFactory.getLogger(TBcmd.class);

    private boolean polling;

    private ZKConfiguration zkConfiguration;

    private Environment environment;
    private EnvironmentConfigs environmentConfigs;

    public TBcmd() {
        try {
            zkConfiguration = requiresZK();
            String environmentName = zkConfiguration.getString(ZKConfigNode.ENVIRONMENT);
            environment = Environment.valueOf(environmentName);
            environmentConfigs = EnvironmentConfigs.valueOf(environmentName);
        } catch (Exception ex) {
            log.error("TBcmd: command creation failed! ", ex);
            System.exit(-1);
        }
    }

    private ZKConfiguration requiresZK() throws IOException {
        ZKConfiguration zkConfiguration = new ZKConfiguration();
        try {
            zkConfiguration.connect();
            zkConfiguration.initial();
        } catch (IOException | KeeperException | InterruptedException ex) {
            throw new IOException("Zookeeper is required for shared configuration!!! ", ex);
        }
        return zkConfiguration;
    }

    @SuppressWarnings("unchecked")
    public void start() {

        ProjectDataManager projectDataManager = new ProjectDataManager(environment, "TBcmd", zkConfiguration);
        KafkaConsumer<String, byte[]> consumer = createConsumer();

        /*TODO: need to load topicBuild from configuration*/
        String topicBuild = KafkaTopics.PROJECT_BUILD.getTopic();
        consumer.subscribe(Collections.singletonList(topicBuild));
        log.info("Subscribed to topicBuild " + topicBuild);

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

                /*TODO: future feature: add command to UpdateProjectCommandQueue*/
                BuildPackageCommand buildPackageCommand = new BuildPackageCommand(offset, key, value, environmentConfigs, projectDataManager);
                log.info("Incoming message: {}", buildPackageCommand.toString());

                /*TODO: future feature: move this execute block into UpdateProjectCommandQueue*/
                try {
                    buildPackageCommand.execute();
                    log.info("Incoming message completed: {}", buildPackageCommand.toString());
                } catch (InvalidParameterException inex) {
                    /*Notice: how to handle rejected command, can rebuild when percentComplete < 100*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
                } catch (UnsupportedOperationException ex) {
                    /*Notice: how to handle rejected command, can rebuild when percentComplete < 100*/
                    log.error("UnsupportedOperationException: {}", ex.getMessage());
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
                } catch (Exception ex) {
                    log.error("Unexpected error occur: ", ex);
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
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

    private KafkaConsumer<String, byte[]> createConsumer() {
        /*TODO: need to load consumer configuration*/
        environment = Environment.DEVELOPMENT;
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "tbcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer<>(props);
    }

    public void stop() {
        polling = false;
    }

}
