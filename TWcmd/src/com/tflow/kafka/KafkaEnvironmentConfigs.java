package com.tflow.kafka;

public enum KafkaEnvironmentConfigs {

    PRODUCTION("com.tflow.kafka.ObjectSerializer", "com.tflow.kafka.ObjectDeserializer"),
    DEVELOPMENT("com.tflow.kafka.JSONSerializer", "com.tflow.kafka.JSONDeserializer");

    String kafkaSerializer;
    String kafkaDeserializer;

    KafkaEnvironmentConfigs(String kafkaSerializer, String kafkaDeserializer) {
        this.kafkaSerializer = kafkaSerializer;
        this.kafkaDeserializer = kafkaDeserializer;
    }

    public String getKafkaSerializer() {
        return kafkaSerializer;
    }

    public String getKafkaDeserializer() {
        return kafkaDeserializer;
    }
}
