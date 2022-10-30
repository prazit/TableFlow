package com.tflow.wcmd;

import com.tflow.file.SerializeReader;
import com.tflow.file.SerializeWriter;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FileUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.helpers.MessageFormatter;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public abstract class IOCommand extends KafkaCommand {

    protected DataManager dataManager;
    protected RecordAttributesData recordAttributes;
    protected PackageMapper mapper;

    public IOCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs, DataManager dataManager) {
        super(offset, key, value, environmentConfigs);
        this.dataManager = dataManager;
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

    protected Date getModifiedDate(ProjectFileType projectFileType, RecordAttributesData additional) {
        File file = getFile(projectFileType, additional);
        if (file.exists()) {
            return new Date(file.lastModified());
        }
        return null;
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

    protected RecordData lastRecordData;

    protected Object getData(ProjectFileType projectFileType, RecordAttributesData recordAttributesData) throws InstantiationException, IOException, ClassNotFoundException {
        File file = getFile(projectFileType, recordAttributesData);
        if (!file.exists()) {
            info("DATA_FILE_NOT_FOUND: {}", file);
            return KafkaErrorCode.DATA_FILE_NOT_FOUND.getCode();
        }

        try {
            lastRecordData = (RecordData) readFrom(file);
            return lastRecordData.getData();
        } catch (ClassCastException ex) {
            return KafkaErrorCode.INVALID_DATA_FILE.getCode();
        }
    }

    protected Object getData(ProjectFileType projectFileType) throws InstantiationException, IOException, ClassNotFoundException {
        return getData(projectFileType, recordAttributes);
    }

    protected Object getData(ProjectFileType projectFileType, String recordId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(recordId);
        return getData(projectFileType, recordAttributesData);
    }

    protected Object getData(ProjectFileType projectFileType, int recordId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        return getData(projectFileType, recordAttributesData);
    }

    protected Object getData(ProjectFileType projectFileType, int recordId, int stepId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        return getData(projectFileType, recordAttributesData);
    }

    protected Object getData(ProjectFileType projectFileType, int recordId, int stepId, int dataTableId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        recordAttributesData.setDataTableId(String.valueOf(dataTableId));
        return getData(projectFileType, recordAttributesData);
    }

    protected Object getData(ProjectFileType projectFileType, int recordId, int stepId, int ignoredId, int transformTableId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        recordAttributesData.setTransformTableId(String.valueOf(transformTableId));
        return getData(projectFileType, recordAttributesData);
    }

    protected List<LocalData> loadLocalDataList() throws IOException, InstantiationException, ClassNotFoundException {
        Object data = getData(ProjectFileType.LOCAL_LIST);
        List<Integer> localIdList = (List<Integer>) throwExceptionOnError(data);

        List<LocalData> localDataList = new ArrayList<>();
        for (Integer id : localIdList) {
            data = getData(ProjectFileType.LOCAL, id);
            localDataList.add((LocalData) throwExceptionOnError(data));
        }

        return localDataList;
    }

    protected List<SFTPData> loadSFTPDataList() throws IOException, InstantiationException, ClassNotFoundException {
        Object data = getData(ProjectFileType.SFTP_LIST);
        List<Integer> sftpIdList = (List<Integer>) throwExceptionOnError(data);

        List<SFTPData> sftpDataList = new ArrayList<>();
        for (Integer id : sftpIdList) {
            data = getData(ProjectFileType.SFTP, id);
            sftpDataList.add((SFTPData) throwExceptionOnError(data));
        }

        return sftpDataList;
    }

    protected List<DatabaseData> loadDatabaseDataList() throws ClassNotFoundException, IOException, InstantiationException {
        Object data = getData(ProjectFileType.DB_LIST);
        List<Integer> dbIdList = (List) throwExceptionOnError(data);

        List<DatabaseData> databaseDataList = new ArrayList<>();
        for (Integer databaseId : dbIdList) {
            data = getData(ProjectFileType.DB, databaseId);
            databaseDataList.add((DatabaseData) throwExceptionOnError(data));
        }

        return databaseDataList;
    }

    protected List<VariableData> loadVariableDataList() throws ClassNotFoundException, IOException, InstantiationException {
        List<VariableData> variableDataList = new ArrayList<>();

        Object data = getData(ProjectFileType.VARIABLE_LIST);
        List<Integer> varIdList = (List<Integer>) throwExceptionOnError(data);

        for (Integer varId : varIdList) {
            data = getData(ProjectFileType.VARIABLE, varId);
            variableDataList.add((VariableData) throwExceptionOnError(data));
        }

        return variableDataList;
    }

    protected List<BinaryFileData> loadVersionedDataList(String codeFilter) throws ClassNotFoundException, IOException, InstantiationException {
        List<BinaryFileData> binaryFileDataList = new ArrayList<>();

        Object data = getData(ProjectFileType.VERSIONED_LIST);
        List<VersionedFileData> versionedFileDataList = (List<VersionedFileData>) throwExceptionOnError(data);

        Versioned versioned;
        for (VersionedFileData versionedFileData : versionedFileDataList) {
            versioned = Versioned.valueOf(versionedFileData.getId());
            if (versioned.getProjectTypeCodes().contains(codeFilter)) {
                data = getData(ProjectFileType.VERSIONED, versioned.getFileId());
                binaryFileDataList.add((BinaryFileData) throwExceptionOnError(data));
            }
        }

        return binaryFileDataList;
    }

    protected List<StepData> loadStepDataList() throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.STEP_LIST);
        List<ItemData> stepIdList = (List) throwExceptionOnError(data);

        List<StepData> stepDataList = new ArrayList<>();
        for (ItemData stepItem : stepIdList) {
            data = getData(ProjectFileType.STEP, stepItem.getId(), stepItem.getId());
            stepDataList.add((StepData) throwExceptionOnError(data));
        }

        return stepDataList;
    }

    protected List<TableFxData> loadTransformationDataList(int stepId, int transformTableId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.TRANSFORMATION_LIST, stepId, 0, transformTableId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<TableFxData> tableFxDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.TRANSFORMATION, id, stepId, 0, transformTableId);
            tableFxDataList.add((TableFxData) throwExceptionOnError(data));
        }
        return tableFxDataList;
    }

    protected List<OutputFileData> loadTransformOutputDataList(int stepId, int transformTableId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, stepId, 0, transformTableId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<OutputFileData> outputFileDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.TRANSFORM_OUTPUT, id, stepId, 0, transformTableId);
            outputFileDataList.add((OutputFileData) throwExceptionOnError(data));
        }
        return outputFileDataList;
    }

    protected List<TransformColumnData> loadTransformColumnDataList(int stepId, int transformTableId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.TRANSFORM_COLUMN_LIST, stepId, 0, transformTableId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<TransformColumnData> transformColumnDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.TRANSFORM_COLUMN, id, stepId, 0, transformTableId);
            transformColumnDataList.add((TransformColumnData) throwExceptionOnError(data));
        }
        return transformColumnDataList;
    }

    protected List<TransformTableData> loadTransformTableDataList(int stepId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.TRANSFORM_TABLE_LIST, 0, stepId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<TransformTableData> transformTableDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.TRANSFORM_TABLE, id, stepId, 0, id);
            transformTableDataList.add((TransformTableData) throwExceptionOnError(data));
        }
        return transformTableDataList;
    }

    protected List<OutputFileData> loadDataOutputDataList(int stepId, int dataTableId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.DATA_OUTPUT_LIST, 0, stepId, dataTableId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<OutputFileData> outputFileDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.DATA_OUTPUT, id, stepId, dataTableId);
            outputFileDataList.add((OutputFileData) throwExceptionOnError(data));
        }
        return outputFileDataList;
    }

    protected List<DataColumnData> loadDataColumnDataList(int stepId, int dataTableId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.DATA_COLUMN_LIST, 0, stepId, dataTableId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<DataColumnData> dataColumnDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.DATA_COLUMN, id, stepId, dataTableId);
            dataColumnDataList.add((DataColumnData) throwExceptionOnError(data));
        }
        return dataColumnDataList;
    }

    protected List<DataTableData> loadDataTableDataList(int stepId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.DATA_TABLE_LIST, 0, stepId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<DataTableData> dataTableDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.DATA_TABLE, id, stepId, id);
            dataTableDataList.add((DataTableData) throwExceptionOnError(data));
        }
        return dataTableDataList;
    }

    protected List<DataFileData> loadDataFileDataList(int stepId) throws InstantiationException, IOException, ClassNotFoundException {
        Object data = getData(ProjectFileType.DATA_FILE_LIST, 0, stepId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        List<DataFileData> dataFileDataList = new ArrayList<>();
        for (Integer id : idList) {
            data = getData(ProjectFileType.DATA_FILE, id, stepId);
            dataFileDataList.add((DataFileData) throwExceptionOnError(data));
        }
        return dataFileDataList;
    }


    protected Object throwExceptionOnError(Object data) throws IOException {
        if (data instanceof Long) {
            throw new IOException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

}
