package com.tflow.model.editor;

public enum DataType {

    STRING("string.png"),
    INTEGER("integer.png"),
    DECIMAL("decimal.png"),
    DATE("date.png"),
    ;

    private String image;

    DataType(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}
