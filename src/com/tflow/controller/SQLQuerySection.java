package com.tflow.controller;

public enum SQLQuerySection {

    QUERY("Query", "pi-database", "queryForm:tabview:queryTab"),
    FILTER("Filter", "pi-filter", "queryForm:tabview:filterTab"),
    SORT("Sort", "pi-sort", "queryForm:tabview:sortTab"),

    SQL("Preview", "pi-code", "queryForm:tabview:sqlTab"),
    ;

    private String title;
    private String icon;
    private String update;


    SQLQuerySection(String title, String icon, String update) {
        this.title = title;
        this.icon = icon;
        this.update = update;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getUpdate() {
        return update;
    }

    public static SQLQuerySection parse(String title) {
        for (SQLQuerySection section : values()) {
            if (title.contains(section.getTitle())) return section;
        }
        return null;
    }
}
