package com.tflow.kafka;

import java.io.Serializable;

public class KafkaRecordValue implements Serializable {
    private static final long serialVersionUID = 2022061609996660002L;

    private Object data;
    private Object additional;

    public KafkaRecordValue(Object data, Object additional) {
        this.data = data;
        this.additional = additional;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getAdditional() {
        return additional;
    }

    public void setAdditional(Object additional) {
        this.additional = additional;
    }

    @Override
    public String toString() {
        return "{" +
                "data:" + data +
                ", additional:" + additional +
                '}';
    }
}
