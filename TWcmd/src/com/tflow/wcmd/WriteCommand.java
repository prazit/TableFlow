package com.tflow.wcmd;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

/**
 * Receive kafka message then convert and write to storage.
 */
public abstract class WriteCommand {

    protected ConsumerRecord<String, String> kafkaRecord;

    public WriteCommand(ConsumerRecord<String, String> kafkaRecord) {
        this.kafkaRecord = kafkaRecord;
    }

    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException;

}
