package com.tflow.model.editor;

public enum DataType {

    STRING("STR", "string.png"),
    INTEGER("INT", "integer.png"),
    DECIMAL("DEC", "decimal.png"),
    DATE("DTE", "date.png"),
    ;

    private String shorten;
    private String image;

    DataType(String shorten, String image) {
        this.shorten = shorten;
        this.image = image;
    }

    public String getShorten() {
        return shorten;
    }

    public String getImage() {
        return image;
    }
}
