package com.tflow.kafka;

import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.data.record.JSONRecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.LoggerFactory;

public class JSONDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        /* for Read Consumer */
        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        Object object = null;
        try {
            object = SerializeUtil.fromTJson(data);
        } catch (Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
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
