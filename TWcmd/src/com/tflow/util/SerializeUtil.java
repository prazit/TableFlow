package com.tflow.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SerializeUtil {

    public static String serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
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

}
