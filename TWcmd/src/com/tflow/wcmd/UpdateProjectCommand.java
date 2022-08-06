package com.tflow.wcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaRecord;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.DateTimeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class UpdateProjectCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(UpdateProjectCommand.class);

    public UpdateProjectCommand(String key, Object value, EnvironmentConfigs environmentConfigs) {
        super(key, value, environmentConfigs);
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException, SerializationException {
        RecordMapper mapper = Mappers.getMapper(RecordMapper.class);
        RecordData recordData = mapper.map((KafkaRecord) this.value);
        ProjectFileType projectFileType = validate(key, recordData);
        RecordAttributesData additional = recordData.getAdditional();

        Date now = DateTimeUtil.now();
        long dateDiffMs = now.getTime() - additional.getModifiedDate().getTime();
        long maxDiff = 50000; //TODO: load maxDiff from configuration.
        if( dateDiffMs < maxDiff ) additional.setModifiedDate(now);

        File file = getFile(projectFileType, additional);
        if (file.exists()) {

            /*move existing Data File to Transaction folder*/
            File historyFile = getHistoryFile(projectFileType, additional);
            RecordData historyRecord = (RecordData) readFrom(file);
            writeTo(historyFile, historyRecord);

            /*need created-info from history*/
            RecordAttributesData historyAdditional = historyRecord.getAdditional();
            additional.setCreatedDate(historyAdditional.getCreatedDate());
            additional.setCreatedUserId(historyAdditional.getCreatedUserId());
            additional.setCreatedClientId(historyAdditional.getCreatedClientId());

        } else {
            additional.setCreatedDate(now);
            additional.setCreatedUserId(additional.getModifiedUserId());
            additional.setCreatedClientId(additional.getModifiedClientId());
        }

        if (recordData.getData() == null) {
            remove(file);
        } else {
            encrypt(recordData);
            writeTo(file, recordData);
        }
    }

    private void encrypt(RecordData recordData) {
        /*need to decrypt/encrypt user and password depend on userEncrypted/passwordEncrypted*/
        Object data = recordData.getData();
        if (data instanceof DatabaseData) {
            DatabaseData databaseData = (DatabaseData) data;
            databaseData.setUser(encrypt(databaseData.getUser()));
            databaseData.setPassword(encrypt(databaseData.getPassword()));

        } else if (data instanceof SFTPData) {
            SFTPData sftpData = (SFTPData) data;
            sftpData.setUser(encrypt(sftpData.getUser()));
            sftpData.setPassword(encrypt(sftpData.getPassword()));
        }
    }

    private String encrypt(String data) {
        /*TODO: need 2 Ways encryption from Data Conversion Project*/
        return data;
    }

    private File getHistoryFile(ProjectFileType projectFileType, RecordAttributesData additional) {
        return getFile(projectFileType, additional, environmentConfigs.getHistoryRootPath(), DateTimeUtil.getStr(additional.getModifiedDate(), "-yyyyddMMHHmmssSSS") + environmentConfigs.getDataFileExt());
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
        RecordAttributesData additional = recordData.getAdditional();
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

        additional.setFileType(fileType);
        normalizeAttributes(additional);

        return fileType;
    }

    private void normalizeAttributes(RecordAttributesData additional) {
        ProjectFileType fileType = additional.getFileType();
        switch (fileType.getRequireType()) {
            case 1: // projectId
                additional.setStepId(null);
                additional.setDataTableId(null);
                additional.setTransformTableId(null);
                break;

            case 2: // projectId, stepId
                additional.setDataTableId(null);
                additional.setTransformTableId(null);
                break;

            case 3: // projectId, stepId, dataTableId
                additional.setTransformTableId(null);
                break;

            case 4: // projectId, stepId, transformTableId
                additional.setDataTableId(null);
                break;

            case 0:
                additional.setProjectId(null);
                additional.setStepId(null);
                additional.setDataTableId(null);
                additional.setTransformTableId(null);
                break;
        }
        if (fileType.getPrefix().endsWith("list")) {
            additional.setRecordId(null);
        }
    }

}
