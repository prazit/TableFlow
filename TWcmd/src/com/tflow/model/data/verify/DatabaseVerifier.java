package com.tflow.model.data.verify;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DatabaseVerifier extends DataVerifier {

    public DatabaseVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        DatabaseData databaseData = (DatabaseData) data;
        int objectId = databaseData.getId();

        String objectName = verifyName(databaseData.getName(), "Database({name})", objectId);

        if (isNullOrEmpty(databaseData.getUrl())) addIssueRequired(objectId, objectName, "url");
        if (isNullOrEmpty(databaseData.getDbms())) addIssueRequired(objectId, objectName, "dbms");
        if (isNullOrEmpty(databaseData.getUser())) addIssueRequired(objectId, objectName, "user");
        if (isNullOrEmpty(databaseData.getPassword())) addIssueRequired(objectId, objectName, "password");
        if (databaseData.getRetry() < 0 || databaseData.getRetry() > 9) addIssueRange(objectId, objectName, "retry", "1-9");

        return !hasIssue;
    }

}
