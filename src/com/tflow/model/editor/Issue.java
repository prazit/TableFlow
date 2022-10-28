package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;

import java.util.Map;

public class Issue extends Item implements Selectable {

    private String description;

    private int stepId;
    private String selectableId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public void setSelectableId(String selectableId) {
        this.selectableId = selectableId;
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
                "id:" + id +
                ", name:'" + name + '\'' +
                ", description:'" + description + '\'' +
                ", stepId:" + stepId +
                ", selectableId:'" + selectableId + '\'' +
                '}';
    }
}
