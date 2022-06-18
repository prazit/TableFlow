package com.tflow.wcmd;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Kafka-Topic: UpdateProject
 * Kafka-Key: shorten record key spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class UpdateProjectCommand extends WriteCommand {

    public UpdateProjectCommand(ConsumerRecord kafkaRecord) {
        super(kafkaRecord);
    }

    @Override
    public void execute() throws UnsupportedOperationException {
        /*TODO: split kafkaValue to 2 parts (real-data and additional)*/
        String[] parts = kafkaRecord.value().toString().split("");

        /*TODO: create Additional Data Object from kafkaValue part 2*/
        /*TODO: validate Additional Data and KafkaKey, then convert to Data File Name (Full Path)*/
        /*TODO: check existing of Data File*/
        /*TODO: move existing Data File to Transaction folder*/
        /*TODO: create and write new Data File*/
    }

}
