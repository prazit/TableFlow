package com.tflow.kafka;

import com.tflow.model.data.record.RecordData;
import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordAttributes;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class JSONSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        /* for Read Producer */
        if (data instanceof byte[]) {
            return (byte[]) data;
        }

        /* for Write Producer, for Read Producer */
        if (data instanceof RecordData) {
            try {
                RecordData recordData = (RecordData) data;
                JSONRecordData jsonData = new JSONRecordData();
                jsonData.setData((String) SerializeUtil.toTJsonString(recordData.getData()));
                jsonData.setAdditional((RecordAttributes) recordData.getAdditional());
                return SerializeUtil.toTJson(jsonData);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        /* for Request Producer, for Commit function */
        try {
            return SerializeUtil.toTJson(data);
        } catch (Exception ex) {
            throw new SerializationException(": ", ex);
        }
    }
}
