package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.ProjectData;
import com.tflow.model.data.StepData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class StepVerifier extends DataVerifier {
    public StepVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        StepData stepData = (StepData) data;
        int objectId = stepData.getId();

        String objectName = verifyName(stepData.getName(), "Step({name})", objectId);

        return !hasIssue;
    }
}
