package com.tflow.model.editor;

import java.io.Serializable;
import java.util.Map;

public class TableFx implements Serializable {
    private static final long serialVersionUID = 2021121709996660043L;

    private String name;
    private FunctionPrototype function;
    private Map<String, Object> paramMap;

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
}
