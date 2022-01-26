package com.tflow.model.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ColumnFx implements Serializable, Selectable, HasEndPlug {
    private static final long serialVersionUID = 2021121709996660042L;

    private int id;
    private String name;
    private ColumnFunction function;
    private Map<String, Object> propertyMap;

    private LinePlug endPlug;
    private LinePlug startPlug;

    private DataColumn owner;

    public ColumnFx(DataColumn owner, ColumnFunction function, String name, String startPlug, String endPlug) {
        this.name = name;
        this.function = function;
        this.endPlug = new EndPlug(endPlug);
        this.startPlug = new StartPlug(startPlug);
        this.owner = owner;
        propertyMap = new HashMap<>();
        function.getProperties().initPropertyMap(propertyMap);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnFunction getFunction() {
        return function;
    }

    public void setFunction(ColumnFunction function) {
        this.function = function;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    @Override
    public LinePlug getEndPlug() {
        return endPlug;
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public LinePlug getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    public DataColumn getOwner() {
        return owner;
    }

    public void setOwner(DataColumn owner) {
        this.owner = owner;
    }

    @Override
    public Properties getProperties() {
        return function.getProperties();
    }

    @Override
    public String getSelectableId() {
        return "cfx" + id;
    }
}
