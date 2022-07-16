package com.tflow.kafka;

public class ProjectDataWriteBuffer {

    private ProjectFileType fileType;
    private Object dataObject;
    private KafkaRecordAttributes additional;

    public ProjectDataWriteBuffer(ProjectFileType fileType, Object dataObject, KafkaRecordAttributes additional) {
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

    public KafkaRecordAttributes getAdditional() {
        return additional;
    }

    public void setAdditional(KafkaRecordAttributes additional) {
        this.additional = additional;
    }
}
