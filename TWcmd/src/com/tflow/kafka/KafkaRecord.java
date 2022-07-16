package com.tflow.kafka;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class KafkaRecord {

    private Object data;
    private KafkaRecordAttributes additional;

    public KafkaRecord() {
        /*nothing*/
    }

    public KafkaRecord(Object data, KafkaRecordAttributes additional) {
        this.data = data;
        this.additional = additional;
    }
}
