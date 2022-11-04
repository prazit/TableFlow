package com.tflow.model.editor.sql;

public class QueryColumn {

    private int id;
    private int index;
    private String name;
    private String value;

    private boolean selected;

    private QueryTable owner;

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
}
