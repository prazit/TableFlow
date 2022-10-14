package com.tflow.model.data.verify;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class SFTPVerifier extends DataVerifier{
    public SFTPVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<String> messageList) {
        if(!(data instanceof SFTPData)) return false;

        /*TODO: verify SFTPData*/

        return true;
    }
}
