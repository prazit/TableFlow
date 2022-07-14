package com.tflow.wcmd;

import com.tflow.file.ReadSerialize;
import com.tflow.file.WriteSerialize;
import com.tflow.kafka.KafkaEnvironmentConfigs;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordData;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.mapper.RecordAttributesMapper;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FileUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.Date;

/*TODO: need to append last command to the list of command in history folder
   put file in the step folder
   split file by max-file-size from configuration*/

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class UpdateProjectCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(UpdateProjectCommand.class);

    public UpdateProjectCommand(String key, Object value, KafkaEnvironmentConfigs kafkaEnvironmentConfigs) {
        super(key, value, kafkaEnvironmentConfigs);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException, SerializationException {
        RecordData recordData = (RecordData) value;
        ProjectFileType projectFileType = validate((String) key, recordData);

        RecordAttributesData additional = (RecordAttributesData) recordData.getAdditional();
        Date now = DateTimeUtil.now();
        additional.setModifiedDate(now);

        File file = getFile(projectFileType, additional);
        if (file.exists()) {

            /*move existing Data File to Transaction folder*/
            File historyFile = getHistoryFile(projectFileType, additional);
            RecordData historyRecord = readFrom(file);
            writeTo(historyFile, historyRecord);

            /*need created-info from history*/
            RecordAttributesData historyAdditional = (RecordAttributesData) historyRecord.getAdditional();
            additional.setCreatedDate(historyAdditional.getCreatedDate());
            additional.setCreatedUserId(historyAdditional.getCreatedUserId());
            additional.setCreatedClientId(historyAdditional.getCreatedClientId());

        } else {
            additional.setCreatedDate(now);
            additional.setCreatedUserId(additional.getModifiedUserId());
            additional.setCreatedClientId(additional.getModifiedClientId());
        }

        if (recordData.getData() == null) {
            log.info("remove( file: {}, additional: {} )", file, additional);
            remove(file);
        } else {
            log.info("writeTo( file: {}, additional: {} )", file, additional);
            writeTo(file, recordData);
        }
    }

    private void remove(File file) {
        try {
            if (!file.delete()) throw new Exception("file.delete() return false.");
        } catch (Exception ex) {
            log.warn("remove( file: {} ) failed! {}", file, ex.getMessage());
        }
    }

    private RecordData readFrom(File file) throws IOException, ClassNotFoundException, InstantiationException {

        RecordData recordData = null;
        FileInputStream fileIn = new FileInputStream(file);

        /*-- normal cast to known object --*/
        ObjectInputStream in = createObjectInputStream(kafkaEnvironmentConfigs.getInputStream(), fileIn);
        ReadSerialize readSerialize = (ReadSerialize) in;
        recordData = (RecordData) readSerialize.readSerialize();
        in.close();
        fileIn.close();

        log.info("readFrom( file: {} ). kafkafRecordValue.additional = {}", file, recordData.getAdditional());
        return recordData;
    }

    /**
     * IMPORTANT: replace only.
     */
    private void writeTo(File file, RecordData recordData) throws IOException, InstantiationException, SerializationException {
        FileUtil.autoCreateParentDir(file);
        FileOutputStream fileOut = new FileOutputStream(file, false);
        ObjectOutputStream objectOutputStream = createObjectOutputStream(kafkaEnvironmentConfigs.getOutputStream(), fileOut);
        ((WriteSerialize) objectOutputStream).writeSerialize(recordData);
        objectOutputStream.close();
        fileOut.close();
    }

    private File getHistoryFile(ProjectFileType projectFileType, RecordAttributesData additional) {
        /*TODO: need history root path from configuration*/
        String rootPath = "/Apps/TFlow/hist";
        return getFile(projectFileType, additional, rootPath, DateTimeUtil.getStr(additional.getModifiedDate(), "-yyyyddMMHHmmssSSS"));
    }

    private File getFile(ProjectFileType projectFileType, RecordAttributesData additional) {
        /*TODO: need project data root path from configuration*/
        String rootPath = "/Apps/TFlow/project";
        return getFile(projectFileType, additional, rootPath, "");
    }

    private File getFile(ProjectFileType projectFileType, RecordAttributesData additional, String rootPath, String postFix) {
        String path;

        switch (projectFileType.getRequireType()) {
            case 2:
                path = "/" + additional.getProjectId() + "/" + additional.getStepId();
                break;

            case 3:
                path = "/" + additional.getProjectId() + "/" + additional.getStepId() + "/" + additional.getDataTableId();
                break;

            case 4:
                path = "/" + additional.getProjectId() + "/" + additional.getStepId() + "/" + additional.getTransformTableId();
                break;

            default: //case 1:
                path = "/" + additional.getProjectId();
        }

        return new File(rootPath + path + "/" + getFileName(projectFileType.getPrefix(), additional.getRecordId()) + postFix);
    }

    private String getFileName(String prefix, String recordId) {
        if (prefix.endsWith("list"))
            return prefix;
        return prefix + recordId;
    }

    /**
     * validate Additional Data and KafkaKey
     **/
    private ProjectFileType validate(String kafkaRecordKey, RecordData recordData) throws UnsupportedOperationException, InvalidParameterException {

        ProjectFileType fileType;
        try {
            fileType = ProjectFileType.valueOf(kafkaRecordKey);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Invalid operation '" + kafkaRecordKey + "', recommends to use value from enum 'ProjectFileType' !!");
        }

        /*check required data for the KafkaKey*/
        RecordAttributes additional = (RecordAttributes) recordData.getAdditional();
        int requireType = fileType.getRequireType();

        // recordId is required on all require types.
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) {
            throw new InvalidParameterException("Additional.RecordId is required for operation('" + fileType.name() + "')");
        }

        // projectId is required on all types.
        if (additional.getProjectId() == null) {
            throw new InvalidParameterException("Additional.ProjectId is required for operation('" + fileType.name() + "')");
        }

        // stepId is required on all types except type(1).
        if (requireType > 1 && additional.getStepId() == null) {
            throw new InvalidParameterException("Additional.StepId is required for operation('" + fileType.name() + "')");
        }

        // dataTableId is required on type 3 only.
        if (requireType == 3 && additional.getDataTableId() == null) {
            throw new InvalidParameterException("Additional.DataTableId is required for operation('" + fileType.name() + "')");
        }

        // transformTableId is required on type 4 only.
        if (requireType == 4 && additional.getTransformTableId() == null) {
            throw new InvalidParameterException("Additional.TransformTableId is required for operation('" + fileType.name() + "')");
        }

        RecordAttributesMapper mapper = Mappers.getMapper(RecordAttributesMapper.class);
        RecordAttributesData recordAttributesData = mapper.map(additional);
        recordAttributesData.setFileType(fileType);
        recordData.setAdditional(recordAttributesData);
        return fileType;
    }


}
