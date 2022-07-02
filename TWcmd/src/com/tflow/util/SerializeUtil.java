package com.tflow.util;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SerializeUtil {

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        //return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] serialize(Long data) {
        return new byte[] {
                (byte) (data >>> 56),
                (byte) (data >>> 48),
                (byte) (data >>> 40),
                (byte) (data >>> 32),
                (byte) (data >>> 24),
                (byte) (data >>> 16),
                (byte) (data >>> 8),
                data.byteValue()
        };
    }

    public static byte[] serializeHeader(long clientId, long statusCode) throws SerializationException {
        byte[] client = serialize(clientId);
        byte[] code = serialize(statusCode);


        byte[] header = new byte[ 16 ];
        System.arraycopy(client, 0, header, 0, 8);
        System.arraycopy(code, 0, header, 8, 8);

        return header;
    }

    public static Object deserialize(String value) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.ISO_8859_1));
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object deserialized = objectInputStream.readObject();
        objectInputStream.close();
        return deserialized;
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object deserialized = objectInputStream.readObject();
        objectInputStream.close();
        return deserialized;
    }

    public static long deserializeHeader(byte[] data) throws SerializationException {
        if (data.length != 16) {
            throw new SerializationException("Size of data received by deserializeLong is not 16");
        }

        long code = deserializeLong(Arrays.copyOfRange(data, 8, 16));
        if (code < 0) {
            return code;
        }

        return deserializeLong(Arrays.copyOfRange(data, 0, 8));
    }

    public static long deserializeLong(byte[] code) {
        long value = 0;
        for (byte b : code) {
            value <<= 8;
            value |= b & 0xFF;
        }
        return value;
    }

}
