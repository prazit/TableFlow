package com.tflow.model.editor;

import java.io.Serializable;
import java.util.Map;

public class ColumnFx implements Serializable, Selectable {
    private static final long serialVersionUID = 2021121709996660042L;

    private DataColumn owner;

    private String name;
    private ColumnFunction function;
    private Map<String, Object> paramMap;

    private String endPlug;
    private String startPlug;

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

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
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
        return owner.getSelectableId() + name.replaceAll("[ ]", "");
    }
}
