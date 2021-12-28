package com.tflow.model.editor;

import com.tflow.model.editor.datasource.SFTP;

public class FTPFile extends DataFile {
    private static final long serialVersionUID = 2021121709996660021L;

    private SFTP sftp;
    private String ftpName;

    public FTPFile(SFTP sftp, String ftpName, DataFileType type, String localName, String localPath, String endPlug, String startPlug) {
        super(type, localName, localPath, endPlug, startPlug);
        this.sftp = sftp;
        this.ftpName = ftpName;
    }

    public SFTP getSftp() {
        return sftp;
    }

    public void setSftp(SFTP sftp) {
        this.sftp = sftp;
    }

    public String getFtpName() {
        return ftpName;
    }

    public void setFtpName(String ftpName) {
        this.ftpName = ftpName;
    }

    @Override
    public Properties getProperties() {
        return Properties.FTP_FILE;
    }
}
