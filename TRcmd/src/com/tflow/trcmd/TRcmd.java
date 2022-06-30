package com.tflow.trcmd;

import com.tflow.wcmd.TWcmd;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TRcmd {

    private Logger log = LoggerFactory.getLogger(TWcmd.class);

    private boolean polling;

    public TRcmd() {
        /*nothing*/
    }

    @SuppressWarnings("unchecked")
    public void start() {
        /*example from: https://www.tutorialspoint.com/apache_kafka/apache_kafka_consumer_group_example.htm*/

        KafkaConsumer<String, Object> consumer = createConsumer();
        KafkaProducer<String, Object> dataProducer = createProducer();

        /*TODO: need to load topic from configuration*/
        String topic = "project-read"; //"quickstart-events";
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic " + topic);

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, Object> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            for (ConsumerRecord<String, Object> record : records) {

                Object value = record.value();
                String key = record.key();
                String offset = String.valueOf(record.offset());
                log.info("Rawdata: offset = {}, key = {}, value = {}", offset, key, value);

                /*TODO: add command to UpdateProjectCommandQueue*/
                ReadProjectCommand readProjectCommand = new ReadProjectCommand(record, dataProducer, topic);

                /*test only*/
                /*TODO: move this execute block into UpdateProjectCommandQueue*/
                try {
                    log.info("readProjectCommand(offset: {}, key: {}) started.", offset, key);
                    readProjectCommand.execute();
                    log.info("readProjectCommand completed.");
                } catch (InvalidParameterException inex) {
                    /*TODO: how to handle rejected command*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.info("readProjectCommand(offset: {}, key: {}) rejected.", offset, key);
                } catch (Exception ex) {
                    log.error("Hard error: ", ex);
                    log.info("readProjectCommand(offset: {}, key: {}) rejected.", offset, key);
                }
            }
        }

        consumer.close();
    }

    private KafkaConsumer<String, Object> createConsumer() {
        /*TODO: need to load consumer configuration*/
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "trcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer.encoding", "UTF-8");
        return new KafkaConsumer<String, Object>(props);
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
        props.put("value.serializer", "com.tflow.kafka.ObjectSerializer");
        return new KafkaProducer<String, Object>(props);
    }

    public void stop() {
        polling = false;
    }

}
