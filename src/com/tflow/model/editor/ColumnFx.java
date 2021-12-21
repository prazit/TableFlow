package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;

import java.io.Serializable;
import java.util.Map;

public class ColumnFx extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660042L;

    private String name;
    private FunctionPrototype function;
    private Map<String, Object> paramMap;

    private String endPlug;
    private String startPlug;

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
}
