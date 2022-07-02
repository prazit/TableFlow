package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class ObjectDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        try {
            return SerializeUtil.deserialize(data);
        } catch (IOException | ClassNotFoundException ex) {
            throw new SerializationException("Error when deserialize byte[] to Object : " + ex.getMessage());
        }
    }

}
