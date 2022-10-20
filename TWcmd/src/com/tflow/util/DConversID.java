package com.tflow.util;

public class DConversID {

    private String name;

    public DConversID(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name.replaceAll("[\\p{Punct}\\s]", "_").replaceAll("_+", "_").toLowerCase();
    }
}
