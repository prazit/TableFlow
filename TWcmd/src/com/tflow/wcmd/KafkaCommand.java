package com.tflow.wcmd;

import com.tflow.kafka.KafkaEnvironmentConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Receive kafka message then deserialize and execute something.
 */
public abstract class KafkaCommand {

    protected String key;
    protected Object value;
    protected KafkaEnvironmentConfigs kafkaEnvironmentConfigs;

    public KafkaCommand(String key, Object value, KafkaEnvironmentConfigs kafkaEnvironmentConfigs) {
        this.key = key;
        this.value = value;
        this.kafkaEnvironmentConfigs = kafkaEnvironmentConfigs;
    }

    protected ObjectOutputStream createObjectOutputStream(String className, FileOutputStream fileOutputStream) throws InstantiationException {
        try {
            Class outputClass = Class.forName(className);
            Constructor constructor = outputClass.getDeclaredConstructor(OutputStream.class);
            return (ObjectOutputStream) constructor.newInstance(fileOutputStream);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new InstantiationException(className + " creation failed, " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    protected ObjectInputStream createObjectInputStream(String className, FileInputStream fileInputStream) throws InstantiationException {
        try {
            Class inputClass = Class.forName(className);
            Constructor constructor = inputClass.getDeclaredConstructor(InputStream.class);
            return (ObjectInputStream) constructor.newInstance(fileInputStream);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new InstantiationException(className + " creation failed, " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException;

}
