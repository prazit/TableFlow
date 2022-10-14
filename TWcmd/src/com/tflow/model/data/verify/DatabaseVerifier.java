package com.tflow.model.data.verify;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class DatabaseVerifier extends DataVerifier {

    public DatabaseVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<String> messageList) {
        if(!(data instanceof DatabaseData)) return false;

        /*TODO: verify DatabaseData*/

        return true;
    }

}
