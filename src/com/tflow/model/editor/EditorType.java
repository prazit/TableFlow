package com.tflow.model.editor;

public enum EditorType {

    STEP("resources/stepMenu.xhtml", "flowchart.xhtml"),
    PROJECT("resources/projectMenu.xhtml", "project.xhtml");

    String page;
    String menu;

    EditorType(String menu, String page) {
        this.page = page;
        this.menu = menu;
    }

    public String getMenu() {
        return menu;
    }

    public String getPage() {
        return page;
    }
}
