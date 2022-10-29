package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class LocalVerifier extends DataVerifier {
    public LocalVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        LocalData localData = (LocalData) data;
        int objectId = localData.getId();

        String objectName = verifyName(localData.getName(), "Local({name})", objectId);
        
        if (isNullOrEmpty(localData.getRootPath())) addIssueRequired(objectId, objectName, "rootPath");

        return !hasIssue;
    }
}
