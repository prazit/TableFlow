package com.tflow.model.editor.datasource;

public class NameValue {

    private String name;
    private String value;

    /*ui-only*/
    private int index;
    private boolean last;

    public NameValue(String name, String value) {
        this.name = name;
        this.value = value;
        this.last = false;
    }

    public NameValue(String name, String value, int index) {
        this.name = name;
        this.value = value;
        this.last = false;
        this.index = index;
    }

    public NameValue() {
        this.last = false;
    }

    public NameValue(boolean last) {
        this.last = last;
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

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "{" +
                "index:'" + index + '\'' +
                ", name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", last:" + last +
                '}';
    }
}
