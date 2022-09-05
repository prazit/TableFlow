package com.tflow.kafka;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class JSONKafkaRecord implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660002L;

    private String dataClass;
    private Object data;
    private KafkaRecordAttributes additional;
}
