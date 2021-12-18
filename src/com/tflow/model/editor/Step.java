package com.tflow.model.editor;

import com.tflow.model.editor.action.Action;

import java.util.List;

public class Step {

    private int id;
    private String name;
    private int index;
    private List<Action> history;
    private List<DataTable> dataList;
    private List<TransformTable> transformList;
    private String outputType;
    private String outputName;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Action> getHistory() {
        return history;
    }

    public void setHistory(List<Action> history) {
        this.history = history;
    }

    public List<DataTable> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataTable> dataList) {
        this.dataList = dataList;
    }

    public List<TransformTable> getTransformList() {
        return transformList;
    }

    public void setTransformList(List<TransformTable> transformList) {
        this.transformList = transformList;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}
