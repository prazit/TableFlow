package com.tflow.controller;

public enum SQLQuerySection {
    
    QUERY("Query", "pi-database"),
    FILTER("Filter", "pi-filter"),
    SORT("Sort", "pi-sort"),

    SQL("SQL", "pi-code"),
    ;

    private String title;
    private String icon;

    SQLQuerySection(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public static SQLQuerySection parse(String title) {
        for (SQLQuerySection section : values()) {
            if (title.contains(section.getTitle())) return section;
        }
        return null;
    }

}
