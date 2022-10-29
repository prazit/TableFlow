package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;
import com.tflow.model.data.TransformTableData;

import java.util.ArrayList;

public class TransformTableVerifier extends DataVerifier {
    public TransformTableVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        TransformTableData transformTableData = (TransformTableData) data;
        int objectId = transformTableData.getId();

        String objectName = verifyName(transformTableData.getName(), "DataTable({name})", objectId);

        if (isNullOrEmpty(transformTableData.getIdColName())) addIssueRequired(objectId, objectName, "idColName");

        return !hasIssue;
    }
}
