package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;

import java.util.Map;

public abstract class SelectableReady implements Selectable {

    @Override
    public ProjectFileType getProjectFileType() {
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
