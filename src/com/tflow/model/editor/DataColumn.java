package com.tflow.model.editor;

public class DataColumn {
    private static final long serialVersionUID = 2021121709996660031L;

    private int index;
    private DataType type;
    private String name;
    private String startPlug;

    public DataColumn(int index, DataType type, String name, String startPlug) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.startPlug = startPlug;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
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
