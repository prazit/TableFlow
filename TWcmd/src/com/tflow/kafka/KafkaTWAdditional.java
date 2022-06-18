package com.tflow.kafka;

import java.io.Serializable;
import java.util.Date;

public class KafkaTWAdditional implements Serializable {
    private static final long serialVersionUID = 2022061609996660001L;

    /* Parent Field Group: all fields are optional */
    private String projectId;
    private String stepId;
    private String dataTableId;
    private String transformTableId;

    /* Transaction Field Group: all fields are required */
    private long modifiedClientId;
    private long modifiedUserId;

    /* Generated Field Group: generate by WriteCommand */
    private long createdClientId;
    private long createdUserId;
    private Date createdDate;
    private Date modifiedDate;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getDataTableId() {
        return dataTableId;
    }

    public void setDataTableId(String dataTableId) {
        this.dataTableId = dataTableId;
    }

    public String getTransformTableId() {
        return transformTableId;
    }

    public void setTransformTableId(String transformTableId) {
        this.transformTableId = transformTableId;
    }

    public long getModifiedClientId() {
        return modifiedClientId;
    }

    public void setModifiedClientId(long modifiedClientId) {
        this.modifiedClientId = modifiedClientId;
    }

    public long getModifiedUserId() {
        return modifiedUserId;
    }

    public void setModifiedUserId(long modifiedUserId) {
        this.modifiedUserId = modifiedUserId;
    }

    public long getCreatedClientId() {
        return createdClientId;
    }

    public void setCreatedClientId(long createdClientId) {
        this.createdClientId = createdClientId;
    }

    public long getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(long createdUserId) {
        this.createdUserId = createdUserId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return "{" +
                "projectId:'" + projectId + '\'' +
                ", stepId:'" + stepId + '\'' +
                ", dataTableId:'" + dataTableId + '\'' +
                ", transformTableId:'" + transformTableId + '\'' +
                ", modifiedClientId:" + modifiedClientId +
                ", modifiedUserId:" + modifiedUserId +
                ", createdClientId:" + createdClientId +
                ", createdUserId:" + createdUserId +
                ", createdDate:" + createdDate +
                ", modifiedDate:" + modifiedDate +
                '}';
    }
}
