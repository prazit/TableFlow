package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;

import java.util.Map;

public interface Selectable {

    ProjectFileType getProjectFileType();

    Properties getProperties();

    String getSelectableId();

    LinePlug getStartPlug();

    void setStartPlug(LinePlug startPlug);

    Map<String, Object> getPropertyMap();

}
