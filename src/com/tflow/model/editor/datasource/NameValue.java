package com.tflow.model.editor.datasource;

public class NameValue {

    private String name;
    private String value;

    /*ui-only*/
    private boolean last;

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

    @Override
    public String toString() {
        return "{" +
                "name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", last:" + last +
                '}';
    }
}
