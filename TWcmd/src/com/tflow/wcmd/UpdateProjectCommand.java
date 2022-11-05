package com.tflow.wcmd;

import com.clevel.dconvers.ngin.Crypto;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaRecord;
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
public class UpdateProjectCommand extends IOCommand {

    private Logger log = LoggerFactory.getLogger(UpdateProjectCommand.class);

    public UpdateProjectCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs) {
        super(offset, key, value, environmentConfigs, null);
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException, SerializationException {
        Date now = DateTimeUtil.now();

        RecordMapper mapper = Mappers.getMapper(RecordMapper.class);
        RecordData recordData = mapper.map((KafkaRecord) this.value);
        ProjectFileType projectFileType = validate(key, recordData, now);
        RecordAttributesData additional = recordData.getAdditional();

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
            writeTo(file, recordData);
        }
    }

    /**
     * validate Additional Data and KafkaKey
     **/
    private ProjectFileType validate(String kafkaRecordKey, RecordData recordData, Date operateDate) throws UnsupportedOperationException, InvalidParameterException {

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
        if (requireType > 0 && additional.getProjectId() == null) {
            throw new InvalidParameterException("Additional.ProjectId is required for operation('" + fileType.name() + "')");
        }

        // stepId is required on all types except type(1).
        if (requireType > 1 && requireType < 9 && additional.getStepId() == null) {
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

        // childId is required ont type 3,4,5,6.
        if (requireType > 2 && requireType < 7 && additional.getChildId() == null) {
            throw new InvalidParameterException("Additional.ChildId is required for operation('" + fileType.name() + "')");
        }

        additional.setFileType(fileType);
        normalizeAttributes(additional);

        if (additional.getModifiedDate() == null) {
            additional.setModifiedDate(operateDate);
        }

        return fileType;
    }

    private void normalizeAttributes(RecordAttributesData additional) {
        ProjectFileType fileType = additional.getFileType();
        switch (fileType.getRequireType()) {
            case 1: // projectId
            case 9: // uploaded, package, generated
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
