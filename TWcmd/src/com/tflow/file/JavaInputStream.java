package com.tflow.file;

import com.tflow.model.data.record.JavaRecordData;
import com.tflow.model.data.record.RecordData;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class JavaInputStream extends ObjectInputStream implements SerializeReader {

    public JavaInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public Object readSerialize() throws IOException, ClassNotFoundException {
        Object object = null;
        try {
            object = readObject();
        } catch (Exception ex) {
            throw new IOException("Deserialize from TJson failed, ", ex);
        }

        /* for Request Consumer */
        if (!(object instanceof JavaRecordData)) {
            return object;
        }

        /* for Write Consumer, for Read Consumer*/
        try {
            JavaRecordData javaRecordData = (JavaRecordData) object;

            RecordData recordData = new RecordData();
            recordData.setData(javaRecordData.getData());
            recordData.setAdditional(javaRecordData.getAdditional());

            return recordData;
        } catch (Error | Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }

    }
}
