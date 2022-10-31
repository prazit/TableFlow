package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;
import com.tflow.model.data.TransformColumnData;

import java.util.ArrayList;

public class TransformColumnVerifier extends DataVerifier {
    public TransformColumnVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        TransformColumnData transformColumnData = (TransformColumnData) data;
        int objectId = transformColumnData.getId();

        String objectName = verifyName(transformColumnData.getName(), "DataTable({name})", objectId);

        if (transformColumnData.isUseDynamic()) {
            if (isNullOrEmpty(transformColumnData.getDynamicExpression())) addIssueRequired(objectId, objectName, "dynamicExpression");
        } else {
            if (transformColumnData.getSourceColumnId() == 0) addIssueRequired(objectId, objectName, "sourceColumnId");
        }

        return !hasIssue;
    }
}
