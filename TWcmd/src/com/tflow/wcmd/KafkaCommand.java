package com.tflow.wcmd;

import com.tflow.file.SerializeReader;
import com.tflow.file.SerializeWriter;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.util.FileUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Receive kafka message then deserialize and execute something.
 */
public abstract class KafkaCommand {

    protected String key;
    protected Object value;
    protected EnvironmentConfigs environmentConfigs;

    public KafkaCommand(String key, Object value, EnvironmentConfigs environmentConfigs) {
        this.key = key;
        this.value = value;
        this.environmentConfigs = environmentConfigs;
    }

    protected OutputStream createOutputStream(String className, FileOutputStream fileOutputStream) throws InstantiationException {
        try {
            Class outputClass = Class.forName(className);
            Constructor constructor = outputClass.getDeclaredConstructor(OutputStream.class);
            return (OutputStream) constructor.newInstance(fileOutputStream);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new InstantiationException(className + " creation failed, " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    protected InputStream createInputStream(String className, FileInputStream fileInputStream) throws InstantiationException {
        try {
            Class inputClass = Class.forName(className);
            Constructor constructor = inputClass.getDeclaredConstructor(InputStream.class);
            return (InputStream) constructor.newInstance(fileInputStream);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new InstantiationException(className + " creation failed, " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }


    protected File getFile(ProjectFileType projectFileType, RecordAttributesData additional) {
        return getFile(projectFileType, additional, environmentConfigs.getProjectRootPath(), environmentConfigs.getDataFileExt());
    }

    protected File getFile(ProjectFileType projectFileType, RecordAttributesData additional, String rootPath, String postFix) {
        String path;

        switch (projectFileType.getRequireType()) {
            case 2:
                path = additional.getProjectId() + "/" + additional.getStepId() + "/";
                break;

            case 3:
                path = additional.getProjectId() + "/" + additional.getStepId() + "/" + additional.getDataTableId() + "/";
                break;

            case 4:
                path = additional.getProjectId() + "/" + additional.getStepId() + "/" + additional.getTransformTableId() + "/";
                break;

            default: //case 1:
                path = additional.getProjectId() + "/";
        }

        return new File(rootPath + path + getFileName(projectFileType.getPrefix(), additional.getRecordId()) + postFix);
    }

    protected String getFileName(String prefix, String recordId) {
        if (prefix.endsWith("list"))
            return prefix;
        return prefix + recordId;
    }


    protected void remove(File file) throws IOException {
        try {
            if (!file.delete()) throw new IOException("remove( file: " + file + " ) failed! file.delete() return false.");
        } catch (Exception ex) {
            throw new IOException("remove( file: " + file + " ) failed!", ex);
        }
    }

    protected Object readFrom(File file) throws IOException, ClassNotFoundException, InstantiationException {

        Object object = null;
        FileInputStream fileIn = new FileInputStream(file);

        /*-- normal cast to known object --*/
        InputStream inputStream = createInputStream(environmentConfigs.getInputStream(), fileIn);
        SerializeReader serializeReader = (SerializeReader) inputStream;
        object = serializeReader.readSerialize();
        inputStream.close();
        fileIn.close();

        return object;
    }

    /**
     * Notice: IMPORTANT: this function perform replace only.
     */
    protected void writeTo(File file, Object object) throws IOException, InstantiationException, SerializationException {
        FileUtil.autoCreateParentDir(file);
        FileOutputStream fileOut = new FileOutputStream(file, false);
        OutputStream outputStream = createOutputStream(environmentConfigs.getOutputStream(), fileOut);
        ((SerializeWriter) outputStream).writeSerialize(object);
        outputStream.close();
        fileOut.close();
    }


    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException;
}
