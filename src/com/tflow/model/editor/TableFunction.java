package com.tflow.model.editor;

public enum TableFunction {

    FILTER("Filter", Properties.TFX_FILTER),
    SORT("Sort", Properties.TFX_SORT);

    private String name;
    private Properties properties;

    TableFunction(String name, Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Properties getProperties() {
        return properties;
    }

}
