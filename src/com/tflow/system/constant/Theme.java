package com.tflow.system.constant;

public enum Theme {

    LIGHT("saga"),
    DARK("luna-amber");

    private String name;

    Theme(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}