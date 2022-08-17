package com.tflow.model.data;

import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;

public class ProjectDataWriteBuffer {

    private int index;
    private ProjectFileType fileType;
    private Object dataObject;
    private KafkaRecordAttributes additional;

    /**
     * @param index need to sort by this index before commit
     */
    public ProjectDataWriteBuffer(int index, ProjectFileType fileType, Object dataObject, KafkaRecordAttributes additional) {
        this.index = index;
        this.fileType = fileType;
        this.dataObject = dataObject;
        this.additional = additional;
    }

    public int getIndex() {
        return index;
    }

    public ProjectFileType getFileType() {
        return fileType;
    }

    public void setFileType(ProjectFileType fileType) {
        this.fileType = fileType;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }

    public KafkaRecordAttributes getAdditional() {
        return additional;
    }

    public void setAdditional(KafkaRecordAttributes additional) {
        this.additional = additional;
    }

    /**
     * IMPORTANT: this toString used as unique key to allow to write last object only on the same key.
     */
    @Override
    public String toString() {
        return "{" +
                "transactionId:" + additional.getModifiedDate().getTime() +
                ", fileType:" + fileType +
                ", recordId:" + additional.getRecordId() +
                '}';
    }
}
