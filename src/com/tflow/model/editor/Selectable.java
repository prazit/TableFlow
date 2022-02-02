package com.tflow.model.editor;

import java.util.Map;

public interface Selectable {

    /*TODO: need Locked-Status(locked mode, unlocked mode) to disable some properties, as example: File-Type cannot change after extract data structure*/

    public Properties getProperties();

    public String getSelectableId();

    public LinePlug getStartPlug();

    public void setStartPlug(LinePlug startPlug);

    public Map<String, Object> getPropertyMap();

}
