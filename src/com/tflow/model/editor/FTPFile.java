package com.tflow.model.editor;

import com.tflow.model.editor.datasource.SFTP;

public class FTPFile extends DataFile {
    private static final long serialVersionUID = 2021121709996660021L;

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
