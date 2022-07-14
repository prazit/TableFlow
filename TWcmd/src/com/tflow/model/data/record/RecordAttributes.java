package com.tflow.model.data.record;

import java.io.Serializable;

public class RecordAttributes implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660001L;

    /* Parent Field Group: all fields are optional */
    private String recordId;
    private String projectId;
    private String stepId;
    private String dataTableId;
    private String transformTableId;

    /* Transaction Field Group: all fields are required */
    private long clientId;
    private long userId;

    public RecordAttributes() {
        /*nothing*/
    }

    public RecordAttributes(long clientId, long userId, String projectId) {
        this.clientId = clientId;
        this.userId = userId;
        this.projectId = projectId;
    }

    public RecordAttributes(long clientId, long userId, String projectId, String recordId) {
        this.clientId = clientId;
        this.userId = userId;
        this.projectId = projectId;
        this.recordId = recordId;
    }

    public RecordAttributes(long clientId, long userId, String projectId, String recordId, String stepId) {
        this.clientId = clientId;
        this.userId = userId;
        this.projectId = projectId;
        this.recordId = recordId;
        this.stepId = stepId;
    }

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

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "{" +
                "projectId:'" + projectId + '\'' +
                ", stepId:'" + stepId + '\'' +
                ", dataTableId:'" + dataTableId + '\'' +
                ", transformTableId:'" + transformTableId + '\'' +
                ", recordId:'" + recordId + '\'' +
                ", modifiedClientId:" + clientId +
                ", modifiedUserId:" + userId +
                '}';
    }
}
