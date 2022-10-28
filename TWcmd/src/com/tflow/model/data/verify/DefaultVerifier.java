package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DefaultVerifier extends DataVerifier {
    public DefaultVerifier(Exception ex, TWData data) {
        super(new ExceptionData(ex, data));
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        ExceptionData exceptionData = (ExceptionData) data;
        Exception exception = ((ExceptionData) data).getException();
        TWData objectData = exceptionData.getData();

        IssueData issueData = new IssueData();
        issueData.setStepId(-1);
        issueData.setObjectId("");
        issueData.setName(exception.getClass().getSimpleName() + ": on " + objectData.getClass().getSimpleName());
        issueData.setDescription(exception.getMessage());
        messageList.add(issueData);
        return false;
    }
}
