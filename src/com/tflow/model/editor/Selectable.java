package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;

import java.util.Map;

public interface Selectable {

    /*TODO: need Locked-Status(locked mode, unlocked mode) to disable some properties, as example: File-Type cannot change after extract data structure*/

    ProjectFileType getProjectFileType();

    Properties getProperties();

    String getSelectableId();

    LinePlug getStartPlug();

    void setStartPlug(LinePlug startPlug);

    Map<String, Object> getPropertyMap();

}
