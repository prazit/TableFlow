package com.tflow.controller;

public enum GroupSection {

    EXISTING_PROJECT("pi pi-folder", " Existing Project", "existingProject"),
    PROJECT_TEMPLATE("pi pi-file", " New Project", "projectTemplate,newProject"),
    ;

    String icon;
    String title;
    String update;

    /**
     * @param icon such as pi-check, pi-save, pi-open (see Prime-Icon for available icons)
     */
    GroupSection(String icon, String title, String update) {
        this.icon = icon;
        this.title = title;
        this.update = update;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getUpdate() {
        return update;
    }
}
