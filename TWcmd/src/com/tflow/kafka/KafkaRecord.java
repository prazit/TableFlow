package com.tflow.kafka;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class KafkaRecord implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660070L;

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
