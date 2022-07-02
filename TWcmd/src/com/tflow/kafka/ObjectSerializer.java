package com.tflow.kafka;

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

        try {
            return SerializeUtil.serialize(data);
        } catch (IOException ex) {
            throw new SerializationException("Error when serializing string to byte[] : " + ex.getMessage());
        }
    }
}
