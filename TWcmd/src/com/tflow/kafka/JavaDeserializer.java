package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JavaDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        Object object;
        try {
            object = SerializeUtil.deserialize(data);
        } catch (IOException | ClassNotFoundException ex) {
            LoggerFactory.getLogger(JavaDeserializer.class).error("", ex);
            throw new SerializationException(": ", ex);
        }

        return object;
    }

}
