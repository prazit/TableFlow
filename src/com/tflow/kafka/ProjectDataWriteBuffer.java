package com.tflow.kafka;

import com.tflow.model.data.record.RecordAttributes;

public class ProjectDataWriteBuffer {

    private ProjectFileType fileType;
    private Object dataObject;
    private RecordAttributes additional;

    public ProjectDataWriteBuffer(ProjectFileType fileType, Object dataObject, RecordAttributes additional) {
        this.fileType = fileType;
        this.dataObject = dataObject;
        this.additional = additional;
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

    public RecordAttributes getAdditional() {
        return additional;
    }

    public void setAdditional(RecordAttributes additional) {
        this.additional = additional;
    }
}
