package com.tflow.wcmd;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

/**
 * Receive kafka message then deserialize and execute something.
 */
public abstract class KafkaCommand {

    protected ConsumerRecord<String, String> kafkaRecord;

    public KafkaCommand(ConsumerRecord<String, String> kafkaRecord) {
        this.kafkaRecord = kafkaRecord;
    }

    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException;

}
