package com.tflow.model.data;

public enum IDPrefix {
    
    VERSIONED(""),
    GROUP(""),
    TEMPLATE("T"),
    PROJECT("P"),
    UPLOADED(""),
    PACKAGE("pk"),
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
    TRANSFORM_TABLE("tt"),
    TRANSFORM_COLUMN(""),
    TRANSFORM_COLUMNFX(""),
    TRANSFORMATION("tfx"),
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
