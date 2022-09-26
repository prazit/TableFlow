package com.tflow.model.editor.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameValue {
    private String name;
    private String value;

    /*ui-only*/
    private boolean last;

    public NameValue(String name, String value) {
        this.name = name;
        this.value = value;
        this.last = false;
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

    @Override
    public String toString() {
        return "{" +
                "name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", last:" + last +
                '}';
    }
}
