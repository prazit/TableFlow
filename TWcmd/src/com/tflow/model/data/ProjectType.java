package com.tflow.model.data;

public enum ProjectType {

    BATCH("B"),

    /*serverless*/
    KAFKA("K"),
    SERVLET("S"),

    /*require web server*/
    WEBUI("U"),
    WEBSERVICE("W"),
    ;

    String code;

    ProjectType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
