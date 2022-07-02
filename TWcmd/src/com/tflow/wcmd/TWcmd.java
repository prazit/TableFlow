package com.tflow.wcmd;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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

        /*TODO: need configuration for consumer*/
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "twcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", StandardCharsets.UTF_8.name());
        props.put("value.deserializer", "com.tflow.kafka.ObjectDeserializer");
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<String, Object>(props);

        String topic = "project-write";
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic " + topic);

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, Object> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            for (ConsumerRecord<String, Object> record : records) {

                /*TODO: add command to UpdateProjectCommandQueue*/
                UpdateProjectCommand updateProjectCommand = new UpdateProjectCommand(record);

                /*test only*/
                /*TODO: move this execute block into UpdateProjectCommandQueue*/
                long offset = record.offset();
                String key = record.key();
                try {
                    log.info("updateProjectCommand(offset: {}, key: {}) started.", offset, key);
                    updateProjectCommand.execute();
                    log.info("updateProjectCommand completed.");
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
