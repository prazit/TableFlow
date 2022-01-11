package com.tflow.model.editor;

import java.io.Serializable;

public class DataColumn implements Serializable, Selectable {
    private static final long serialVersionUID = 2021121709996660031L;

    private int id;
    protected int index;
    protected DataType type;
    protected String name;

    protected String startPlug;

    private DataTable owner;

    public DataColumn(int index, DataType type, String name, String startPlug, DataTable owner) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.startPlug = startPlug;
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_COLUMN;
    }

    @Override
    public String getSelectableId() {
        return "dc" + id;
    }

    public DataTable getOwner() {
        return owner;
    }

    public void setOwner(DataTable owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "DataColumn{" +
                "selectableId=" + ((Selectable) this).getSelectableId() +
                "index=" + index +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
