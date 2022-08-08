package com.tflow.model.data;

public enum IDPrefix {
    
    VERSIONED(""),
    GROUP(""),
    PROJECT(""),
    UPLOADED(""),
    PACKAGE(""),
    GENERATED(""),
    STEP("step"),
    DB("db"),
    SFTP("ftp"),
    LOCAL(""),
    DS(""),
    VARIABLE(""),
    DATA_SOURCE_SELECTOR(""),
    DATA_FILE(""),
    DATA_TABLE("dt"),
    DATA_COLUMN(""),
    DATA_OUTPUT(""),
    TRANSFORM_TABLE(""),
    TRANSFORM_COLUMN(""),
    TRANSFORM_COLUMNFX(""),
    TRANSFORMATION(""),
    TRANSFORM_OUTPUT(""),
    ;

    String prefix;

    IDPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}