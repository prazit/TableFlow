package com.tflow.kafka;

public enum KafkaEnvironmentConfigs {

    PRODUCTION("com.tflow.kafka.ObjectSerializer", "com.tflow.kafka.ObjectDeserializer", "java.io.ObjectInputStream", "java.io.ObjectOutputStream"),
    DEVELOPMENT("com.tflow.kafka.JSONSerializer", "com.tflow.kafka.JSONDeserializer", "com.tflow.file.JSONInputStream", "com.tflow.file.JSONOutputStream");

    private String kafkaSerializer;
    private String kafkaDeserializer;

    private String inputStream;
    private String outputStream;

    KafkaEnvironmentConfigs(String kafkaSerializer, String kafkaDeserializer, String inputStream, String outputStream) {
        this.kafkaSerializer = kafkaSerializer;
        this.kafkaDeserializer = kafkaDeserializer;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public String getKafkaSerializer() {
        return kafkaSerializer;
    }

    public String getKafkaDeserializer() {
        return kafkaDeserializer;
    }

    public String getInputStream() {
        return inputStream;
    }

    public String getOutputStream() {
        return outputStream;
    }
}
