package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.IssueType;

import java.util.Map;

public class Issue implements Selectable {

    private int id;
    private IssueType type;

    private int stepId;
    private ProjectFileType objectType;
    private String objectId;
    private String propertyVar;

    private String display;

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ProjectFileType getObjectType() {
        return objectType;
    }

    public void setObjectType(ProjectFileType objectType) {
        this.objectType = objectType;
    }

    public String getPropertyVar() {
        return propertyVar;
    }

    public void setPropertyVar(String propertyVar) {
        this.propertyVar = propertyVar;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return Properties.ISSUE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.ISSUE.getPrefix() + id;
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
        return null;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", stepId=" + stepId +
                ", objectId='" + objectId + '\'' +
                ", propertyVar='" + propertyVar + '\'' +
                '}';
    }
}
