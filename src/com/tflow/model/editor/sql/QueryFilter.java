package com.tflow.model.editor.sql;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.query.QueryFilterConnector;
import com.tflow.model.data.query.QueryFilterOperation;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;

import java.util.Map;

public class QueryFilter implements Selectable {

    private int id;
    private int index;

    private QueryFilterConnector connector;
    private String leftValue;
    private QueryFilterOperation operation;
    private String rightValue;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public QueryFilterConnector getConnector() {
        return connector;
    }

    public void setConnector(QueryFilterConnector connector) {
        this.connector = connector;
    }

    public String getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(String leftValue) {
        this.leftValue = leftValue;
    }

    public QueryFilterOperation getOperation() {
        return operation;
    }

    public void setOperation(QueryFilterOperation operation) {
        this.operation = operation;
    }

    public String getRightValue() {
        return rightValue;
    }

    public void setRightValue(String rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
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
