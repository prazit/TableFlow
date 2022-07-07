package com.tflow.model.editor;

import java.util.Map;

public class SelectableIdOnly implements Selectable {

    private String selectableId;

    public SelectableIdOnly(String selectableId) {
        this.selectableId = selectableId;
    }

    public void setSelectableId(String selectableId) {
        this.selectableId = selectableId;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getSelectableId() {
        return selectableId;
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }
}
