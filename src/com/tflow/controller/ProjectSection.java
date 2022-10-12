package com.tflow.controller;

public enum ProjectSection {

    DATA_SOURCE("Data Sources"),
    VARIABLE("Variables"),
    UPLOADED("Uploaded Files"),
    VERSIONED("Library Files"),
    PACKAGE("Packages")
    ;

    String title;

    ProjectSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ProjectSection parse(String title) {
        for (ProjectSection section : values()) {
            if (section.title.compareTo(title) == 0) return section;
        }
        return null;
    }
}
