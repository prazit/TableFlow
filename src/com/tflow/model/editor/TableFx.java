package com.tflow.model.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TableFx implements Serializable, Selectable {
    private static final long serialVersionUID = 2021121709996660043L;

    private int id;
    private String name;
    private TableFunction function;
    private Map<String, Object> paramMap;

    private TransformTable owner;

    public TableFx(TableFunction function, String name, TransformTable owner) {
        this.name = name;
        this.function = function;
        paramMap = new HashMap<>();
        this.owner = owner;
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

    public TableFunction getFunction() {
        return function;
    }

    public void setFunction(TableFunction function) {
        this.function = function;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public TransformTable getOwner() {
        return owner;
    }

    public void setOwner(TransformTable owner) {
        this.owner = owner;
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
        return new HashMap<>();
    }

    @Override
    public Properties getProperties() {
        return function.getProperties();
    }

    @Override
    public String getSelectableId() {
        return "tfx" + id;
    }
}
