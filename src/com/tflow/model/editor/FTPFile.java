package com.tflow.model.editor;

public class FTPFile extends DataFile {

    private String ftpName;

    public FTPFile(String ftpName, DataFileType type, String localPath, String endPlug, String startPlug) {
        super(type, localPath, endPlug, startPlug);
        this.ftpName = ftpName;
    }

    public String getFtpName() {
        return ftpName;
    }

    public void setFtpName(String ftpName) {
        this.ftpName = ftpName;
    }

}
