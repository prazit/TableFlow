package com.tflow.model.editor;

public enum ColumnFunction {

    LOOKUP("Lookup", Properties.CFX_LOOKUP),
    GET("Get Value", Properties.CFX_GET),
    CONCAT("Concatenation", Properties.CFX_CONCAT),
    ROWCOUNT("Row Count", Properties.CFX_ROWCOUNT),
    ;

    private String name;
    private Properties properties;

    ColumnFunction(String name, Properties properties) {
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
