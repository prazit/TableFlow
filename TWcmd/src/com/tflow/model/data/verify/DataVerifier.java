package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public abstract class DataVerifier {

    private ArrayList<IssueData> issueList;
    private TWData data;

    public ArrayList<IssueData> getIssueList() {
        return issueList;
    }

    public DataVerifier(TWData data) {
        this.data = data;
    }

    public boolean verify() {
        issueList = new ArrayList<>();
        return verifyData(data, issueList);
    }

    protected abstract boolean verifyData(TWData data, ArrayList<IssueData> issueList);

}
