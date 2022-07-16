package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class JavaSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        if (data instanceof byte[]) {
            return (byte[]) data;
        }

        try {
            return SerializeUtil.serialize(data);
        } catch (Exception ex) {
            throw new SerializationException(": ", ex);
        }
    }
}
