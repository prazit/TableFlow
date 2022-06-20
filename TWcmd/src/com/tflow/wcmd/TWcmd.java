package com.tflow.wcmd;

import com.tflow.kafka.KafkaRecordValue;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
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
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "twcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer.encoding", "UTF-8");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        String topic = "quickstart-events";
        consumer.subscribe(Arrays.asList(topic));
        log.info("Subscribed to topic " + topic);

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, String> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            for (ConsumerRecord<String, String> record : records) {

                /*TODO: remove test script block*/
                {
                    String value = record.value();
                    log.info("Rawdata: offset = {}, key = {}, value = {}", record.offset(), record.key(), value);

                    testWriteSerialized(record.value().getBytes(StandardCharsets.ISO_8859_1));

                    try {
                        log.info("Deserialize: value = {}", SerializeUtil.deserialize(value));
                    } catch (Exception ex) {
                        log.error("Deserialize Failed! ", ex);
                    }
                }

                /*TODO: add command to Queue*/
                UpdateProjectCommand updateProjectCommand = new UpdateProjectCommand(record);

                /*test without Queue, execute the command*/
                try {
                    updateProjectCommand.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        consumer.close();
    }

    public void testWriteSerialized(byte[] serialized) {
        try {
            FileOutputStream fileOut = new FileOutputStream("/Apps/TFlow/TestConsumerSerialize.ser");
            fileOut.write(serialized);
            fileOut.close();
            log.info("testWriteSerialized: Serialized data is saved in /Apps/TFlow/TestConsumerSerialize.ser");
        } catch (IOException i) {
            log.error("testWriteSerialized failed,", i);
        }
    }


    public void stop() {
        polling = false;
    }

}
