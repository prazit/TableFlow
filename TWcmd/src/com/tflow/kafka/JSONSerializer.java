package com.tflow.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JSONSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        if (data instanceof byte[]) {
            return (byte[]) data;
        }

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(data).getBytes(StandardCharsets.ISO_8859_1);
        } catch (Exception ex) {
            throw new SerializationException("Error when serializing to byte[] : " + ex.getMessage());
        }
    }
}
