package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class BinaryFileVerifier extends DataVerifier {
    public BinaryFileVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        return true;
    }
}
