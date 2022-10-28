package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class SFTPVerifier extends DataVerifier{
    public SFTPVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> messageList) {
        if(!(data instanceof SFTPData)) return false;

        /*TODO: verify SFTPData*/

        return true;
    }
}
