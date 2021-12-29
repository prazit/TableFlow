package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.SFTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private String name;
    private Batch batch;
    private int activeStepIndex;
    private List<Step> stepList;
    private Map<String, DataSource> dataSourceList;
    private Map<String, SFTP> sftpList;
    private Map<String, Variable> variableList;

    private int lastElementId;
    private int lastUniqueId;

    public Project(String name) {
        this.name = name;
        stepList = new ArrayList<>();
        dataSourceList = new HashMap<>();
        sftpList = new HashMap<>();
        variableList = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public int getActiveStepIndex() {
        return activeStepIndex;
    }

    public void setActiveStepIndex(int activeStepIndex) {
        this.activeStepIndex = activeStepIndex;
    }

    public List<Step> getStepList() {
        return stepList;
    }

    public void setStepList(List<Step> stepList) {
        this.stepList = stepList;
    }

    public Map<String, DataSource> getDataSourceList() {
        return dataSourceList;
    }

    public void setDataSourceList(Map<String, DataSource> dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public Map<String, SFTP> getSftpList() {
        return sftpList;
    }

    public void setSftpList(Map<String, SFTP> sftpList) {
        this.sftpList = sftpList;
    }

    public Map<String, Variable> getVariableList() {
        return variableList;
    }

    public void setVariableList(Map<String, Variable> variableList) {
        this.variableList = variableList;
    }

    /*== Public Methods ==*/

    /**
     * Generate new element id (unique within the project)
     *
     * @return String elementId
     */
    public String newElementId() {
        return "em" + (++lastElementId);
    }

    public int newUniqueId() {
        return ++lastUniqueId;
    }

    public Step getActiveStep() {
        if (activeStepIndex < 0) return null;
        return stepList.get(activeStepIndex);
    }
}
