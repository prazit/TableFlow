package com.tflow.kafka;

/*TODO: read all configs from specified file*/
public enum EnvironmentConfigs {

    PRODUCTION("com.tflow.kafka.JavaSerializer", "com.tflow.kafka.JavaDeserializer", "java.io.JavaInputStream", "java.io.JavaOutputStream", "/Apps/TFlow/project/", "/Apps/TFlow/hist/", "/Apps/TFlow/bin/", 3600, ""),
    DEVELOPMENT("com.tflow.kafka.JSONSerializer", "com.tflow.kafka.JSONDeserializer", "com.tflow.file.JSONInputStream", "com.tflow.file.JSONOutputStream", "/Apps/TFlow/project/", "/Apps/TFlow/hist/", "/Apps/TFlow/bin/", 3600, ".json");

    private String kafkaSerializer;
    private String kafkaDeserializer;

    private String inputStream;
    private String outputStream;

    private String projectRootPath;
    private String historyRootPath;
    private String binaryRootPath;
    private String dataFileExt;

    private long clientFileTimeoutMs;

    /*TODO: change arguments to single file-name argument*/
    /*TODO: need function to load configs from specified file, any app need to call this once at startup*/
    EnvironmentConfigs(String kafkaSerializer, String kafkaDeserializer, String inputStream, String outputStream, String projectRootPath, String historyRootPath, String binaryRootPath, long clientFileTimeoutMs, String dataFileExt) {
        this.kafkaSerializer = kafkaSerializer;
        this.kafkaDeserializer = kafkaDeserializer;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.projectRootPath = projectRootPath;
        this.historyRootPath = historyRootPath;
        this.binaryRootPath = binaryRootPath;
        this.clientFileTimeoutMs = clientFileTimeoutMs;
        this.dataFileExt = dataFileExt;
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

    public String getProjectRootPath() {
        return projectRootPath;
    }

    public String getHistoryRootPath() {
        return historyRootPath;
    }

    public String getBinaryRootPath() {
        return binaryRootPath;
    }

    public long getClientFileTimeoutMs() {
        return clientFileTimeoutMs;
    }

    public String getDataFileExt() {
        return dataFileExt;
    }
}
