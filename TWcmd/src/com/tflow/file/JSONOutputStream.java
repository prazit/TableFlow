package com.tflow.file;

import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JSONOutputStream extends ObjectOutputStream implements WriteSerialize {

    public JSONOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public void writeSerialize(Object obj) throws IOException, SerializationException {

        if (obj instanceof RecordData) {
            try {
                RecordData recordData = (RecordData) obj;

                JSONRecordData jsonData = new JSONRecordData();
                jsonData.setData((String) SerializeUtil.toTJsonString(recordData.getData()));
                jsonData.setAdditional((RecordAttributes) recordData.getAdditional());

                write(SerializeUtil.toTJson(jsonData));
            } catch (Error | Exception ex) {
                throw new SerializationException(": ", ex);
            }
            return;
        }

        /* for Request Producer, for Commit function */
        write(SerializeUtil.toTJson(obj));

    }
}
