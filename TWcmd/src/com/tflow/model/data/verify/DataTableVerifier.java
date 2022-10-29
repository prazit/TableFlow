package com.tflow.model.data.verify;

import com.tflow.model.data.DataTableData;
import com.tflow.model.data.IssueData;
import com.tflow.model.data.StepData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DataTableVerifier extends DataVerifier {
    public DataTableVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        DataTableData dataTableData = (DataTableData) data;
        int objectId = dataTableData.getId();

        String objectName = verifyName(dataTableData.getName(), "DataTable({name})", objectId);

        if (isNullOrEmpty(dataTableData.getIdColName())) addIssueRequired(objectId, objectName, "idColName");

        return !hasIssue;
    }
}
