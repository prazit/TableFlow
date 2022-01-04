package com.tflow.model.editor;

import com.tflow.model.editor.datasource.Database;
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
    private Map<Integer, Database> databaseMap;
    private Map<Integer, SFTP> sftpMap;
    private Map<String, Variable> variableMap;

    private int lastElementId;
    private int lastUniqueId;

    public Project(String name) {
        activeStepIndex = -1;
        this.name = name;
        stepList = new ArrayList<>();
        databaseMap = new HashMap<>();
        sftpMap = new HashMap<>();
        variableMap = new HashMap<>();
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

    public Map<Integer, Database> getDatabaseMap() {
        return databaseMap;
    }

    public void setDatabaseMap(Map<Integer, Database> databaseMap) {
        this.databaseMap = databaseMap;
    }

    public Map<Integer, SFTP> getSftpMap() {
        return sftpMap;
    }

    public void setSftpMap(Map<Integer, SFTP> sftpMap) {
        this.sftpMap = sftpMap;
    }

    public Map<String, Variable> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(Map<String, Variable> variableMap) {
        this.variableMap = variableMap;
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
