package com.tflow.model.editor;

import java.util.Map;

public interface Selectable {

    public Properties getProperties();

    public String getSelectableId();

    public LinePlug getStartPlug();

    public void setStartPlug(LinePlug startPlug);

    public Map<String, Object> getPropertyMap();

}
