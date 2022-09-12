package com.tflow.controller;

public enum Page {

    DEFAULT("index.xhtml", "Redirecting", ""),

    GROUP("group.xhtml", "Open Project", "some description here"),
    EDITOR("editor.xhtml", "Project Editor", "project editor"),

    PLAYGROUND("playground.xhtml", "Play Ground", "playground work only in Development Environment, test anything you want in the playground.");

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
