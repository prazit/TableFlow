package com.tflow.model.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TableFx implements Serializable, Selectable {
    private static final long serialVersionUID = 2021121709996660043L;

    private String name;
    private FunctionPrototype function;
    private Map<String, Object> paramMap;

    private TransformTable owner;

    public TableFx(String name, FunctionPrototype function, TransformTable owner) {
        this.name = name;
        this.function = function;
        paramMap = new HashMap<>();
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionPrototype getFunction() {
        return function;
    }

    public void setFunction(FunctionPrototype function) {
        this.function = function;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    @Override
    public Properties getProperties() {
        return Properties.TABLE_FX;
    }

    @Override
    public String getSelectableId() {
        if (name == null) return "";
        return owner.getSelectableId() + name.replaceAll("[ ]", "_");
    }
}
