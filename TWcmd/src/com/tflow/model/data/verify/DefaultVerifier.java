package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DefaultVerifier extends DataVerifier {
    public DefaultVerifier(Exception ex) {
        super(new ExceptionData(ex));
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<String> messageList) {
        ExceptionData exceptionData = (ExceptionData) data;
        messageList.add(((ExceptionData) data).getException().getMessage());
        return false;
    }
}
