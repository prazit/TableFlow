package com.tflow.model.editor;

import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.Tower;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Step {

    private int id;
    private String name;
    private int index;
    private List<Action> history;
    private List<DataTable> dataList;
    private List<TransformTable> transformList;
    private List<DataOutput> outputList;

    private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;
    private List<Line> lineList;

    private Project owner;

    public Step(int id, String name, int index, Project owner) {
        this.id = id;
        this.name = name;
        this.index = index;
        history = new ArrayList<>();
        dataList = new ArrayList<>();
        transformList = new ArrayList<>();
        outputList = new ArrayList<>();
        dataTower = new Tower(3, this);
        transformTower = new Tower(2, this);
        outputTower = new Tower(2, this);
        lineList = new ArrayList<>();
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

    public List<DataOutput> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<DataOutput> outputList) {
        this.outputList = outputList;
    }

    public Tower getDataTower() {
        return dataTower;
    }

    public void setDataTower(Tower dataTower) {
        this.dataTower = dataTower;
    }

    public Tower getTransformTower() {
        return transformTower;
    }

    public void setTransformTower(Tower transformTower) {
        this.transformTower = transformTower;
    }

    public Tower getOutputTower() {
        return outputTower;
    }

    public void setOutputTower(Tower outputTower) {
        this.outputTower = outputTower;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public Project getOwner() {
        return owner;
    }
}
