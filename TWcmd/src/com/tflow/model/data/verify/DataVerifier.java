package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;

import java.util.ArrayList;

public abstract class DataVerifier {

    private ArrayList<String> messageList;
    private TWData data;

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public DataVerifier(TWData data) {
        this.data = data;
    }

    public boolean verify() {
        messageList = new ArrayList<>();
        return verifyData(data, messageList);
    }

    protected abstract boolean verifyData(TWData data, ArrayList<String> messageList);

}
