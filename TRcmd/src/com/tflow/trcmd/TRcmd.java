package com.tflow.trcmd;

import com.tflow.kafka.KafkaTopics;
import com.tflow.model.data.DataManager;
import com.tflow.system.CLIbase;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import com.tflow.zookeeper.AppName;
import com.tflow.zookeeper.AppsHeartbeat;
import com.tflow.zookeeper.ZKConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TRcmd extends CLIbase {

    public TRcmd() {
        super(AppName.DATA_READER);
    }

    @Override
    protected void loadConfigs() throws Exception {
        /*add more Fixed configuration for Consumer*/
        configs.put("consumer.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("consumer.key.deserializer.encoding", StandardCharsets.UTF_8.name());
        configs.put("consumer.value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        /*add more Fixed configuration for Producer*/
        configs.put("producer.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("producer.key.serializer.encoding", StandardCharsets.UTF_8.name());
        configs.put("producer.value.serializer", environmentConfigs.getKafkaSerializer());
    }

    @SuppressWarnings("unchecked")
    public void run() {

        DataManager dataManager = new DataManager(environment, getClass().getSimpleName(), zkConfiguration);
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(getProperties("consumer.", configs));
        KafkaProducer<String, Object> dataProducer = new KafkaProducer<>(getProperties("producer.", configs));

        /*don't load readTopic from configuration, unchangeable topic by the TFlow-System.*/
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

            if (records.count() == 0) continue;

            terminateOnNewerVersion();

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

                /*TODO: add command to UpdateProjectCommandQueue (BlockingQueue)*/
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
                } finally {
                    dataManager.waitAllTasks();
                }

            }
        }

        consumer.close();
    }

}
