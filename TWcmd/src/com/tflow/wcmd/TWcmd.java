package com.tflow.wcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaTopics;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TWcmd {

    private Logger log = LoggerFactory.getLogger(TWcmd.class);

    private boolean polling;

    public TWcmd() {
        /*nothing*/
    }

    public void start() {
        /*example from: https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm*/
        Properties props = new Properties();

        /*TODO: need configuration for all values below*/
        EnvironmentConfigs environmentConfigs = EnvironmentConfigs.DEVELOPMENT;
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "twcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", StandardCharsets.UTF_8.name());
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);

        String topic = KafkaTopics.PROJECT_WRITE.getTopic();
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic " + topic);

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
            try {
                records = consumer.poll(duration);
            } catch (RecordDeserializationException ex) {
                log.error("Kafka Internal Error: ", ex);
                continue;
            }

            for (ConsumerRecord<String, byte[]> record : records) {

                Object value;
                String key = record.key();
                long offset = record.offset();
                log.info("Incoming message offset: {}, key: {}.", offset, key);

                try {
                    value = deserializer.deserialize(topic, record.value());
                } catch (Exception ex) {
                    log.warn("Skip invalid message={}", new String(record.value(), StandardCharsets.ISO_8859_1));
                    log.warn("Deserialize error: ", ex);
                    continue;
                }

                /*TODO: add command to UpdateProjectCommandQueue*/
                UpdateProjectCommand updateProjectCommand = new UpdateProjectCommand(key, value, environmentConfigs);

                /*TODO: move this execute block into UpdateProjectCommandQueue*/
                try {
                    updateProjectCommand.execute();
                    log.info("updateProjectCommand completed.");

                    /*TODO: IMPORTANT: after success need to commit consumer-group-offset to know its already done to avoid duplicated commands*/

                } catch (InvalidParameterException inex) {
                    /*TODO: how to handle rejected command*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.info("updateProjectCommand(offset: {}, key: {}) rejected.", offset, key);
                } catch (Exception ex) {
                    log.error("Hard error: ", ex);
                    log.info("updateProjectCommand(offset: {}, key: {}) rejected.", offset, key);
                }

            }
        }

        consumer.close();
    }

    public void testWriteSerialized(byte[] serialized) {
        try {
            FileOutputStream fileOut = new FileOutputStream("/Apps/TFlow/tmp/TestConsumerSerialize.ser");
            fileOut.write(serialized);
            fileOut.close();
            log.info("testWriteSerialized: Serialized data is saved in /Apps/TFlow/tmp/TestConsumerSerialize.ser");
        } catch (IOException i) {
            log.error("testWriteSerialized failed,", i);
        }
    }


    public void stop() {
        polling = false;
    }

}
