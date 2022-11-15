package com.tflow.model.editor.room;

public enum RoomType {

    DATA_SOURCE("local.png"),
    DATA_FILE("markdown.png"),
    DATA_TABLE("markdown.png"),
    COLUMN_FX_TABLE("string.png"),
    TRANSFORM_TABLE("markdown.png"),
    EMPTY(""),
    QUERY_TABLE("");

    private String image;

    RoomType(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}
