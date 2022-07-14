package com.tflow.file;

import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/* TODO: need to create JavaInputStream & JavaOutputStream */
public class JSONInputStream extends ObjectInputStream implements ReadSerialize {
    public JSONInputStream(InputStream in) throws IOException {
        super(in);

    }

    @Override
    public Object readSerialize() throws IOException, ClassNotFoundException {
        Object object = null;
        try {
            object = SerializeUtil.fromTJson(readAllBytes());
        } catch (Exception ex) {
            throw new IOException("Deserialize from TJson failed, ", ex);
        }

        /* for Request Consumer */
        if (!(object instanceof JSONRecordData)) {
            return object;
        }

        /* for Write Consumer, for Read Consumer*/
        try {
            JSONRecordData jsonRecordData = (JSONRecordData) object;
            Object dataObject = SerializeUtil.fromTJsonString(jsonRecordData.getData());
            return new RecordData(dataObject, jsonRecordData.getAdditional());
        } catch (Error | Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }


    }
}
