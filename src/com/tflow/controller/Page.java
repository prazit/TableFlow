package com.tflow.controller;

public enum Page {

    GROUP("group.xhtml", "Open Project", "some description here"),
    EDITOR("editor.xhtml", "Project", "project editor"),
    ;

    String name;
    String title;
    String description;

    Page(String name, String title, String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
