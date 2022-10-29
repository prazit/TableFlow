package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DataColumnVerifier extends DataVerifier {
    public DataColumnVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        return !hasIssue;
    }
}
