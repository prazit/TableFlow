package com.tflow.model.editor;

public enum DataCharset {

    UTF8("UTF-8"),
    WINDOWNS1252("Windows-1252"),
    ;

    private String charset;

    DataCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }
}
