package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;
import com.tflow.model.data.TableFxData;
import com.tflow.model.data.TransformColumnData;

import java.util.ArrayList;

public class TableFxVerifier extends DataVerifier {
    public TableFxVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        TableFxData tableFxData = (TableFxData) data;
        int objectId = tableFxData.getId();

        String objectName = verifyName(tableFxData.getName(), "DataTable({name})", objectId);

        //if (isNullOrEmpty(tableFxData.getDynamicExpression())) addIssueRequired(objectId, objectName, "dynamicExpression");

        return !hasIssue;
    }
}
