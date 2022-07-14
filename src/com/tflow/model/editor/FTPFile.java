package com.tflow.model.editor;

import com.tflow.model.editor.datasource.SFTP;

public class FTPFile extends DataFile {

    private String ftpName;

    public FTPFile(SFTP sftp, String ftpName, DataFileType type, String localName, String localPath, String endPlug, String startPlug) {
        super(sftp, type, localName, localPath, endPlug, startPlug);
        this.ftpName = ftpName;
    }

    public String getFtpName() {
        return ftpName;
    }

    public void setFtpName(String ftpName) {
        this.ftpName = ftpName;
    }

}
