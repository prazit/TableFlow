package com.tflow.model.editor;

import java.util.Map;

/*TODO: need solution to show plugs of sourceColumns (more than one plug)*/
public class ColumnFxPlug extends LinePlug implements Selectable {

    private String name;

    public ColumnFxPlug(String plug, String name) {
        super(plug);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Properties getProperties() {
        return Properties.FX_PARAM;
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
