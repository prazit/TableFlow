package com.tflow.model.editor.datasource;

public enum DataSourceType {

    DATABASE("database.png"),
    SFTP("sftp.png"),
    LOCAL("local.png"),
    SYSTEM("environment.png"),
    ;

    private String image;

    DataSourceType(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}
