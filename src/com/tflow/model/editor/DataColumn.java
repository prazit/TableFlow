package com.tflow.model.editor;

public class DataColumn {
    private static final long serialVersionUID = 2021121709996660031L;

    private String type;
    private String name;
    private String startPlug;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }
}
