package com.tflow.file;

import com.google.gson.Gson;
import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JSONInputStream extends DataInputStream implements SerializeReader {

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

        /* for Write Consumer */
        JSONRecordData jsonRecordData = (JSONRecordData) object;
        Class dataClass = Class.forName(jsonRecordData.getDataClass());
        Gson gson = SerializeUtil.getGson();
        try {
            String dataJson = gson.toJson(jsonRecordData.getData());
            Object dataObject = gson.fromJson(dataJson, dataClass);

            RecordData recordData = new RecordData();
            recordData.setData(dataObject);
            recordData.setAdditional(jsonRecordData.getAdditional());

            return recordData;
        } catch (Error | Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }

    }
}
