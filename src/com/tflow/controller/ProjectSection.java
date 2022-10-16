package com.tflow.controller;

public enum ProjectSection {

    DATA_SOURCE("Data Sources","pi-database"),
    VARIABLE("Variables","pi-code"),
    UPLOADED("Uploaded Files","pi-upload"),
    VERSIONED("Library Files","pi-server"),
    PACKAGE("Packages","pi-building")
    ;

    private String title;
    private String icon;

    ProjectSection(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public static ProjectSection parse(String title) {
        for (ProjectSection section : values()) {
            if (title.contains(section.getTitle())) return section;
        }
        return null;
    }
}
