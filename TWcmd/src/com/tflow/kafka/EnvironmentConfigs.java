package com.tflow.kafka;

public enum EnvironmentConfigs {

    PRODUCTION(
            "com.tflow.kafka.JavaSerializer",
            "com.tflow.kafka.JavaDeserializer",
            "java.io.JavaInputStream",
            "java.io.JavaOutputStream",
            "/Apps/TFlow/project/",
            "/Apps/TFlow/hist/",
            "/Apps/TFlow/bin/",
            36000,
            "",
            777777777
    ),

    DEVELOPMENT(
            "com.tflow.kafka.JSONSerializer",
            "com.tflow.kafka.JSONDeserializer",
            "com.tflow.file.JSONInputStream",
            "com.tflow.file.JSONOutputStream",
            "/Apps/TFlow/project/",
            "/Apps/TFlow/hist/",
            "/Apps/TFlow/bin/",
            360000,
            ".json",
            777777777
    );

    /* specific values can't replace by CLI Switches */
    private String kafkaSerializer;
    private String kafkaDeserializer;

    private String inputStream;
    private String outputStream;

    private String dataFileExt;

    /* default values can replace by CLI Switches */
    private String projectRootPath;
    private String historyRootPath;
    private String binaryRootPath;

    private long clientFileTimeoutMs;

    private int templateGroupId;

    EnvironmentConfigs(String kafkaSerializer, String kafkaDeserializer, String inputStream, String outputStream, String projectRootPath, String historyRootPath, String binaryRootPath, long clientFileTimeoutMs, String dataFileExt, int templateGroupId) {
        this.kafkaSerializer = kafkaSerializer;
        this.kafkaDeserializer = kafkaDeserializer;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.projectRootPath = projectRootPath;
        this.historyRootPath = historyRootPath;
        this.binaryRootPath = binaryRootPath;
        this.clientFileTimeoutMs = clientFileTimeoutMs;
        this.dataFileExt = dataFileExt;
        this.templateGroupId = templateGroupId;
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

    public int getTemplateGroupId() {
        return templateGroupId;
    }
}
