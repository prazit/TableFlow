package com.tflow.kafka;

public class ProjectWriteCommand {

    private ProjectFileType fileType;
    private Object dataObject;
    private KafkaTWAdditional additional;

    public ProjectWriteCommand(ProjectFileType fileType, Object dataObject, KafkaTWAdditional additional) {
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

    public KafkaTWAdditional getAdditional() {
        return additional;
    }

    public void setAdditional(KafkaTWAdditional additional) {
        this.additional = additional;
    }
}
