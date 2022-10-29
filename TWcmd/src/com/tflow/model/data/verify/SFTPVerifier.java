package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class SFTPVerifier extends DataVerifier {
    public SFTPVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        SFTPData sftpData = (SFTPData) data;
        int objectId = sftpData.getId();

        String objectName = verifyName(sftpData.getName(), "SFTP({name})", objectId);

        if (isNullOrEmpty(sftpData.getHost())) addIssueRequired(objectId, objectName, "host");
        if (sftpData.getPort() == 0) addIssueRequired(objectId, objectName, "port");

        if (isNullOrEmpty(sftpData.getUser())) addIssueRequired(objectId, objectName, "user");
        if (isNullOrEmpty(sftpData.getPassword())) addIssueRequired(objectId, objectName, "password");
        if (sftpData.getRetry() < 0 || sftpData.getRetry() > 9) addIssueRange(objectId, objectName, "retry", "1-9");

        if (isNullOrEmpty(sftpData.getRootPath())) addIssueRequired(objectId, objectName, "rootPath");
        if (isNullOrEmpty(sftpData.getTmp())) addIssueRequired(objectId, objectName, "tmp");

        return !hasIssue;
    }


}
