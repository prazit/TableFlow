package com.tflow.model.editor.sql;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;

import java.util.Map;

public class QuerySort implements Selectable {

    private int id;
    private int index;
    private String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getSelectableId() {
        return null;
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {

    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }
}
