package com.tflow.trcmd;

import com.tflow.kafka.*;
import com.tflow.model.data.record.ClientRecordData;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.SerializeUtil;
import com.tflow.wcmd.KafkaCommand;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class ReadProjectCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(ReadProjectCommand.class);

    private String topic;
    private KafkaProducer<String, Object> dataProducer;
    private RecordMapper mapper;

    public ReadProjectCommand(String key, Object value, EnvironmentConfigs environmentConfigs, KafkaProducer<String, Object> dataProducer, String topic) {
        super(key, value, environmentConfigs);
        this.dataProducer = dataProducer;
        this.topic = topic;
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException {
        mapper = Mappers.getMapper(RecordMapper.class);
        RecordAttributesData additional = mapper.map((KafkaRecordAttributes) value);

        ProjectFileType projectFileType;
        try {
            projectFileType = validate(key, additional);
        } catch (InvalidParameterException ex) {
            KafkaErrorCode kafkaErrorCode = KafkaErrorCode.valueOf(ex.getMessage());
            sendObject(key, additional.getModifiedClientId(), kafkaErrorCode.getCode());
            log.warn("Invalid parameter: {}", kafkaErrorCode);
            return;
        } catch (UnsupportedOperationException ex) {
            sendObject(key, additional.getModifiedClientId(), KafkaErrorCode.UNSUPPORTED_FILE_TYPE.getCode());
            log.warn(ex.getMessage());
            return;
        }

        /*support open new project from template (projectId starts with "T")*/
        if (ProjectFileType.PROJECT.equals(projectFileType) && isTemplate(additional.getProjectId())) {
            String projectId = copyTemplateToNewProject(additional);
            additional.setProjectId(projectId);
        }

        File file = getFile(projectFileType, additional);
        if (!file.exists()) {
            sendObject(key, additional.getModifiedClientId(), KafkaErrorCode.DATA_FILE_NOT_FOUND.getCode());
            log.warn("File not found: {}", file.getAbsolutePath());
            return;
        }

        File clientFile = getClientFile(additional);
        if (clientFile.exists()) {
            ClientRecordData clientRecordData = (ClientRecordData) readFrom(clientFile);
            if (isExpired(clientRecordData.getExpiredDate())) {
                /*create clientFile at the first read*/
                writeNewClientTo(clientFile, additional);

            } else if (!isMyClient(clientRecordData, additional)) {
                sendObject(key, additional.getModifiedClientId(), KafkaErrorCode.PROJECT_EDITING_BY_ANOTHER.getCode());
                log.warn("Project editing by another: {}", clientRecordData);
                return;
            }

        } else {
            /*create clientFile at the first read*/
            writeNewClientTo(clientFile, additional);
        }

        /*create Data message*/
        RecordData recordValue = (RecordData) readFrom(file);

        /*send Header message and then Data message*/
        sendObject(key, additional.getModifiedClientId(), 0);
        sendObject(key, recordValue);
    }

    private void writeNewClientTo(File clientFile, RecordAttributesData additional) throws IOException, InstantiationException {
        ClientRecordData newClientRecordData = mapper.toClientRecordData(additional);
        newClientRecordData.setExpiredDate(getMilli(environmentConfigs.getClientFileTimeoutMs()));
        writeTo(clientFile, newClientRecordData);
    }

    private long getMilli(long plusMilliSeconds) {
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(plusMilliSeconds);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private boolean isExpired(long expiredDate) {
        return expiredDate < getMilli(0);
    }

    public boolean isMyClient(ClientRecordData clientRecordData, RecordAttributesData additional) {
        return additional.getModifiedClientId() == clientRecordData.getClientId() &&
                additional.getModifiedUserId() == clientRecordData.getUserId();
    }

    private File getClientFile(RecordAttributesData additional) {
        return new File(environmentConfigs.getProjectRootPath() + additional.getProjectId() + "/client" + environmentConfigs.getDataFileExt());
    }

    private String copyTemplateToNewProject(RecordAttributesData additional) {

        /*TODO: read Project List, create new ProjectID (ProjectList will updated by TWcmd)*/

        /*TODO: read Template Project (file by file)*/

        /*TODO: send message to TWcmd to write new project*/

        return "P2";
    }

    private boolean isTemplate(String projectId) {
        return projectId.startsWith("T");
    }

    private void sendObject(String key, long clientId, long statusCode) {
        dataProducer.send(new ProducerRecord<>(topic, key, SerializeUtil.serializeHeader(clientId, statusCode)));
    }

    private void sendObject(String key, Object object) {
        Object record;
        if (object instanceof RecordData) {
            RecordData recordData = (RecordData) object;
            record = new KafkaRecord(recordData.getData(), mapper.map(recordData.getAdditional()));
        } else {
            record = object;
        }
        dataProducer.send(new ProducerRecord<>(topic, key, record));
    }

    /**
     * validate Additional Data and KafkaKey
     **/
    private ProjectFileType validate(String kafkaRecordKey, RecordAttributesData additional) throws UnsupportedOperationException, InvalidParameterException {

        ProjectFileType fileType;
        try {
            fileType = ProjectFileType.valueOf(kafkaRecordKey);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Invalid operation '" + kafkaRecordKey + "', recommends to use value from enum 'ProjectFileType' !!");
        }

        /*check required data for the KafkaKey*/
        int requireType = fileType.getRequireType();

        // recordId is required on all require types.
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_RECORD_ID.name());
        }

        // projectId is required on all types.
        if (additional.getProjectId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_PROJECT_ID.name());
        }

        // stepId is required on all types except type(1).
        if (requireType > 1 && additional.getStepId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_STEP_ID.name());
        }

        // dataTableId is required on type 3 only.
        if (requireType == 3 && additional.getDataTableId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_DATATABLE_ID.name());
        }

        // transformTableId is required on type 4 only.
        if (requireType == 4 && additional.getTransformTableId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_TRANSFORMTABLE_ID.name());
        }

        return fileType;
    }


}
