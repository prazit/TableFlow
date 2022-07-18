package com.tflow.model.editor;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private String id;
    private String name;
    private Batch batch;
    private int activeStepIndex;

    private List<Step> stepList;

    private Map<Integer, Database> databaseMap;
    private Map<Integer, SFTP> sftpMap;
    private Map<Integer, Local> localMap;

    private Map<String, Variable> variableMap;

    private int lastElementId;
    private int lastUniqueId;

    private transient Workspace owner;

    private transient ProjectDataManager manager;

    /*for ProjectMapper*/
    public Project() {
        stepList = new ArrayList<>();
    }

    public Project(String id, String name) {
        this.id = id;
        activeStepIndex = -1;
        this.name = name;
        stepList = new ArrayList<>();
        databaseMap = new HashMap<>();
        sftpMap = new HashMap<>();
        localMap = new HashMap<>();
        variableMap = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

        Step activeStep = getActiveStep();
        if (activeStep != null && activeStep.getIndex() >= 0)
            activeStep.refresh();
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

    public Map<Integer, Local> getLocalMap() {
        return localMap;
    }

    public void setLocalMap(Map<Integer, Local> localMap) {
        this.localMap = localMap;
    }

    public Map<String, Variable> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(Map<String, Variable> variableMap) {
        this.variableMap = variableMap;
    }

    public Workspace getOwner() {
        return owner;
    }

    public void setOwer(Workspace workspace) {
        this.owner = workspace;
    }

    public ProjectDataManager getManager() {
        return manager;
    }

    public void setManager(ProjectDataManager manager) {
        this.manager = manager;
    }

    public int getLastElementId() {
        return lastElementId;
    }

    public void setLastElementId(int lastElementId) {
        this.lastElementId = lastElementId;
    }

    public int getLastUniqueId() {
        return lastUniqueId;
    }

    public void setLastUniqueId(int lastUniqueId) {
        this.lastUniqueId = lastUniqueId;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", name:'" + name + '\'' +
                ", batch:" + batch +
                ", activeStepIndex:" + activeStepIndex +
                ", stepList:" + stepList +
                ", databaseMap:" + databaseMap +
                ", sftpMap:" + sftpMap +
                ", localMap:" + localMap +
                ", variableMap:" + variableMap +
                ", lastElementId:" + lastElementId +
                ", lastUniqueId:" + lastUniqueId +
                '}';
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
        if (activeStepIndex < 0 || activeStepIndex >= stepList.size()) return null;
        return stepList.get(activeStepIndex);
    }

}
