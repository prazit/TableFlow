package com.tflow.kafka;

public class KafkaReadValue {

    private long code;
    private KafkaRecordValue data;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public KafkaRecordValue getData() {
        return data;
    }

    public void setData(KafkaRecordValue data) {
        this.data = data;
    }
}
