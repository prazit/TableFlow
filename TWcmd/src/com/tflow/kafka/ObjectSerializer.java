package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * When data is Long that will known as Read-Header message, otherwise are Serialized String.
 */
public class ObjectSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        if (data instanceof Long) {
            Long longValue = (Long) data;
            return new byte[]{
                    (byte) (longValue >>> 56),
                    (byte) (longValue >>> 48),
                    (byte) (longValue >>> 40),
                    (byte) (longValue >>> 32),
                    (byte) (longValue >>> 24),
                    (byte) (longValue >>> 16),
                    (byte) (longValue >>> 8),
                    longValue.byteValue()
            };
        }

        try {
            return SerializeUtil.serialize(data).getBytes(StandardCharsets.ISO_8859_1);
        } catch (IOException ex) {
            throw new SerializationException("Error when serializing string to byte[] : " + ex.getMessage());
        }
    }
}
