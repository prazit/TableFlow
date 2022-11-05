package com.tflow.kafka;

import java.io.Serializable;
import java.util.Date;

public class KafkaRecordAttributes implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660001L;

    /* Parent Field Group: all fields are optional
     * childId is replacement of dataTableId and transformTableId, store multiple ids separated by slash.
     * Relationship between projectId, stepId, stepChildId and recordId is Data-Path like this: /projectId/stepId/childId/recordId
     */
    private String projectId;
    private String stepId;
    private String childId;
    private String recordId;

    /*Informal deprecated*/
    private String dataTableId;
    private String transformTableId;

    /* Transaction Field Group: all fields are required */
    private long transactionId;
    private long clientId;
    private long userId;
    private Date modifiedDate;

    public KafkaRecordAttributes() {
        /*nothing*/
    }

    public KafkaRecordAttributes(long clientId, long userId, String projectId) {
        this.clientId = clientId;
        this.userId = userId;
        this.projectId = projectId;
    }

    public KafkaRecordAttributes(long clientId, long userId, String projectId, String recordId) {
        this.clientId = clientId;
        this.userId = userId;
        this.projectId = projectId;
        this.recordId = recordId;
    }

    public KafkaRecordAttributes(long clientId, long userId, String projectId, String recordId, String stepId) {
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

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "{" +
                "transactionId:" + transactionId +
                ", time:" + (modifiedDate == null ? "null" : modifiedDate.getTime()) +
                ", userId:" + userId +
                ", clientId:" + clientId +
                ", projectId:'" + projectId + '\'' +
                ", stepId:'" + stepId + '\'' +
                ", dataTableId:'" + dataTableId + '\'' +
                ", transformTableId:'" + transformTableId + '\'' +
                ", childId:'" + childId + '\'' +
                ", recordId:'" + recordId + '\'' +
                '}';
    }
}
