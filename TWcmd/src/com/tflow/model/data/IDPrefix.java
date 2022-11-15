package com.tflow.model.data;

public enum IDPrefix {

    ISSUE("issue"),
    VERSIONED("lib"),
    UPLOADED("up"),
    GENERATED("gen"),
    GROUP("G"),
    TEMPLATE("T"),
    PROJECT("P"),
    PACKAGE("pk"),
    STEP("step"),
    DB("db"),
    SFTP("ftp"),
    LOCAL("lo"),
    VARIABLE("var"),
    DATA_FILE("df"),
    DATA_TABLE("dt"),
    DATA_COLUMN("dc"),
    DATA_OUTPUT("do"),
    TRANSFORM_TABLE("tt"),
    TRANSFORM_COLUMN("tc"),
    TRANSFORMATION("tfx"),
    TRANSFORM_OUTPUT("to"),

    DS(""),
    DATA_SOURCE_SELECTOR(""),
    TRANSFORM_COLUMNFX(""),

    QUERY("q"),
    QUERY_TABLE("qt"),
    QUERY_COLUMN("qc"),
    QUERY_FILTER("qf"),
    QUERY_SORT("qs"),
    QUERY_PREVIEW("qp");

    String prefix;

    IDPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
