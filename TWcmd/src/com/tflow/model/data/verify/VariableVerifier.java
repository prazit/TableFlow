package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.TWData;
import com.tflow.model.data.VariableData;

import java.util.ArrayList;

public class VariableVerifier extends DataVerifier {
    public VariableVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        VariableData variableData = (VariableData) data;
        int objectId = variableData.getId();

        String objectName = verifyName(variableData.getName(), "Variable({name})", objectId);
        
        if (isNullOrEmpty(variableData.getValue())) addIssueRequired(objectId, objectName, "value");

        return !hasIssue;
    }
}
