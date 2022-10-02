package com.tflow.wcmd;

import com.tflow.file.SerializeReader;
import com.tflow.file.SerializeWriter;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FileUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.helpers.MessageFormatter;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class IOCommand extends KafkaCommand {

    public IOCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs) {
        super(offset, key, value, environmentConfigs);
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


    protected File getHistoryFile(ProjectFileType projectFileType, RecordAttributesData additional) {
        String fileName = getFileName(projectFileType.getPrefix(), additional.getRecordId());
        File file = getFile(projectFileType, additional, environmentConfigs.getHistoryRootPath(), "/" + fileName + DateTimeUtil.getStr(additional.getModifiedDate(), "-yyyyddMMHHmmssSSS") + environmentConfigs.getDataFileExt());
        info("DEBUG: historyFile:{} returned from getHistoryFile(projectFileType:{}, additional:{})", file, projectFileType, additional);
        return file;
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

            case 9:
                path = additional.getProjectId() + "/" + projectFileType.name().split("[_]")[0].toLowerCase() + "/";
                break;

            case 0:
                path = projectFileType.name().split("[_]")[0].toLowerCase() + "/";
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
        info("remove(file: {})", file);
        try {
            boolean needHouseKeeper = file.getName().split("[.]")[0].equals(file.getParentFile().getName());
            if (!file.delete()) throw new IOException("remove( file: " + file + " ) failed! file.delete() return false.");

            /*if the file is parent folder (id == parentFolder.name) need to delete child automatic (move to history)*/
            if (needHouseKeeper) removeChilds(file.getParentFile());
        } catch (Exception ex) {
            throw new IOException("remove( file: " + file + " ) failed!", ex);
        }
    }

    protected void removeChilds(File file) throws IOException {
        info("removeChilds(file: {})", file);
        StringBuilder errorMessage = new StringBuilder("");

        List<File> dirList = new ArrayList<>(Collections.singleton(file));
        File[] files;
        File dir;
        boolean noChild;
        while (dirList.size() > 0) {
            dir = dirList.remove(0);
            noChild = true;

            files = dir.listFiles();
            if (files != null) for (File child : files) {

                if (child.isDirectory()) {
                    dirList.add(child);
                    continue;
                }

                /*auto save history before remove*/
                RecordData historyRecord = null;
                try {
                    historyRecord = (RecordData) readFrom(child);
                } catch (Exception ex) {
                    noChild = false;
                    errorMessage.append(MessageFormatter.format("remove(child:{}) : read failed! {}\n", child, ex.getMessage()));
                    continue;
                }

                RecordAttributesData additional = historyRecord.getAdditional();
                File historyFile = getHistoryFile(additional.getFileType(), additional);
                try {
                    writeTo(historyFile, historyRecord);
                } catch (Exception ex) {
                    noChild = false;
                    errorMessage.append(MessageFormatter.format("remove(child:{}) : write failed! {}\n", child, ex.getMessage()));
                    continue;
                }

                try {
                    remove(child);
                } catch (Exception ex) {
                    noChild = false;
                    errorMessage.append(MessageFormatter.format("remove(child:{}) failed! {}\n", child, ex.getMessage()));
                    continue;
                }

            } // end for

            if (noChild) {
                try {
                    remove(dir);
                } catch (Exception ex) {
                    errorMessage.append(MessageFormatter.format("remove(dir:{}) failed! {}\n", dir, ex.getMessage()));
                }
            }

        } // end while

        if (errorMessage.length() != 0) {
            throw new IOException(errorMessage.toString());
        }
    }

    protected Object readFrom(File file) throws IOException, ClassNotFoundException, InstantiationException {
        info("readFrom(file:{})", file);

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
     * Notice: this function perform replace only.
     */
    protected void writeTo(File file, Object object) throws IOException, InstantiationException, SerializationException {
        info("writeTo(file:{})", file);

        FileUtil.autoCreateParentDir(file);
        FileOutputStream fileOut = new FileOutputStream(file, false);
        OutputStream outputStream = createOutputStream(environmentConfigs.getOutputStream(), fileOut);
        ((SerializeWriter) outputStream).writeSerialize(object);
        outputStream.close();
        fileOut.close();
    }

}
