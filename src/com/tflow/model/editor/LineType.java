package com.tflow.model.editor;

public enum LineType {

    TABLE("tLine"),
    STRING("sLine"),
    INTEGER("iLine"),
    DECIMAL("dLine"),
    DATE("dtLine"),
    ;

    private String jsVar;

    LineType(String jsVar) {
        this.jsVar = jsVar;
    }

    public String getJsVar() {
        return jsVar;
    }
}
