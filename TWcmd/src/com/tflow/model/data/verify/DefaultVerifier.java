package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        String simpleName = objectData.getClass().getSimpleName();
        simpleName = simpleName.substring(0, simpleName.length() - 4);

        Logger log = LoggerFactory.getLogger(getClass());
        log.error("No {}Verifier for {}Data", simpleName, simpleName);
        log.trace("", exception);

        return true;
    }
}
