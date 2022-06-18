package com.tflow.wcmd;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Receive kafka message then convert and write to storage.
 */
public abstract class WriteCommand {

    protected ConsumerRecord kafkaRecord;

    public WriteCommand(ConsumerRecord kafkaRecord) {
        this.kafkaRecord = kafkaRecord;
    }

    public abstract void execute() throws UnsupportedOperationException;

}
