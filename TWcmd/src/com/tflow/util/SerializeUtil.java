package com.tflow.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SerializeUtil {

    private static Gson gson;

    private static void initGson() {
        if (gson == null)
            gson = new GsonBuilder()
                    .setDateFormat("dd/MM/yyyy HH:mm:ss.SSSZ")
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                    .setPrettyPrinting()
                    .create();
    }

    public static Gson getGson() {
        initGson();
        return gson;
    }

    public static Deserializer getDeserializer(String deserializerClassName) throws Exception {
        Class deserializerClass = Class.forName(deserializerClassName);
        Constructor constructor = deserializerClass.getConstructor();
        return (Deserializer) constructor.newInstance();
    }

    public static Serializer getSerializer(String serializerClassName) throws Exception {
        Class serializerClass = Class.forName(serializerClassName);
        Constructor constructor = serializerClass.getConstructor();
        return (Serializer) constructor.newInstance();
    }

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        //return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] serialize(Long data) {
        return new byte[]{
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


        byte[] header = new byte[16];
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

    public static String toTJsonString(Object object) {
        initGson();
        return (object == null) ? null : object.getClass().getName() + "=" + gson.toJson(object);
    }

    public static Object fromTJsonString(String tJsonString) throws Exception, Error {
        initGson();
        if (tJsonString == null) return null;

        String[] words = tJsonString.split("[=]", 2);

        Object object = null;
        try {
            Class objectClass = Class.forName(words[0]);
            object = gson.fromJson(words[1], objectClass);
        } catch (Error | Exception ex) {
            LoggerFactory.getLogger(SerializeUtil.class).error("fromTJson error: ", ex);
            throw ex;
        }

        return object;
    }

    public static byte[] toTJson(Object object) {
        initGson();
        if (object == null) return null;
        String tJsonString = object.getClass().getName() + "=" + gson.toJson(object);
        return tJsonString.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static Object fromTJson(byte[] tJson) throws Exception, Error {
        initGson();
        if (tJson == null) return null;

        String tJsonString = new String(tJson, StandardCharsets.ISO_8859_1);
        String[] words = tJsonString.split("[=]", 2);

        Object object = null;
        try {
            Class objectClass = Class.forName(words[0]);
            object = gson.fromJson(words[1], objectClass);
        } catch (Error | Exception ex) {
            LoggerFactory.getLogger(SerializeUtil.class).error("fromTJson error: ", ex);
            throw ex;
        }

        return object;
    }
}
