package com.tflow.kafka;

import com.tflow.model.data.record.JavaRecordData;
import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

public class ObjectSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        if (data instanceof byte[]) {
            return (byte[]) data;
        }

        if (data instanceof RecordData) {
            try {
                RecordData recordData = (RecordData) data;
                JavaRecordData javaRecordData = new JavaRecordData();
                javaRecordData.setData((byte[]) SerializeUtil.serialize(recordData.getData()));
                javaRecordData.setAdditional((RecordAttributes) recordData.getAdditional());
                return SerializeUtil.serialize(javaRecordData);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        try {
            return SerializeUtil.serialize(data);
        } catch (Exception ex) {
            throw new SerializationException(": ", ex);
        }
    }
}
