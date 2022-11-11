package com.tflow.model.editor.sql;

import com.tflow.model.data.query.ColumnType;

public class QueryColumn {

    private int id;
    private int index;
    private ColumnType type;
    private String name;
    private String value;

    private boolean selected;

    private QueryTable owner;

    /*for Mapper*/
    public QueryColumn() {
        /*nothing*/
    }

    public QueryColumn(int index, int id, String name, QueryTable owner) {
        this.index = index;
        this.id = id;
        this.name = name;
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

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public QueryTable getOwner() {
        return owner;
    }

    public void setOwner(QueryTable owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "{" +
                "index:" + index +
                ", id:" + id +
                ", type:'" + type + '\'' +
                ", name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", selected:" + selected +
                ", owner:" + (owner == null ? "none" : owner.getName()) +
                '}';
    }
}
