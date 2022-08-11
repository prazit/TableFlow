package com.tflow.kafka;

public enum KafkaTopics {

    PROJECT_BUILD,
    PROJECT_WRITE,
    PROJECT_READ,
    PROJECT_DATA,
    ;

    public String getTopic() {
        return name().toLowerCase().replaceAll("[_]", "-");
    }

}
