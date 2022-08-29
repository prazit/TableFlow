package com.tflow.controller;

public enum ProjectSection {

    VARIABLE("Variables"),
    DATA_SOURCE("Data Sources"),
    UPLOADED("Uploaded Files"),
    PACKAGE("Packages"),
    ;

    String title;

    ProjectSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
