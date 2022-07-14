package com.tflow.trcmd;

import com.tflow.file.ReadSerialize;
import com.tflow.file.WriteSerialize;
import com.tflow.kafka.*;
import com.tflow.model.data.record.ClientRecordData;
import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.FileUtil;
import com.tflow.util.SerializeUtil;
import com.tflow.wcmd.KafkaCommand;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class ReadProjectCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(ReadProjectCommand.class);

    private String topic;
    private KafkaProducer<String, Object> dataProducer;

    /*TODO: need to load rootPath from configuration*/
    private String rootPath = "/Apps/TFlow/project";

    public ReadProjectCommand(String key, Object value, KafkaEnvironmentConfigs kafkaEnvironmentConfigs, KafkaProducer<String, Object> dataProducer, String topic) {
        super(key, value, kafkaEnvironmentConfigs);
        this.dataProducer = dataProducer;
        this.topic = topic;
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException {
        RecordAttributes additional = (RecordAttributes) value;

        ProjectFileType projectFileType;
        try {
            projectFileType = validate(key, additional);
        } catch (InvalidParameterException ex) {
            KafkaErrorCode kafkaErrorCode = KafkaErrorCode.valueOf(ex.getMessage());
            sendObject(key, additional.getClientId(), kafkaErrorCode.getCode());
            log.warn("Invalid parameter: {}", kafkaErrorCode);
            return;
        } catch (UnsupportedOperationException ex) {
            sendObject(key, additional.getClientId(), KafkaErrorCode.UNSUPPORTED_FILE_TYPE.getCode());
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
            sendObject(key, additional.getClientId(), KafkaErrorCode.DATA_FILE_NOT_FOUND.getCode());
            log.warn("File not found: {}", file.getName());
            return;
        }

        File clientFile = getClientFile(additional);
        if (clientFile.exists()) {
            ClientRecordData clientRecordData = readClientFrom(clientFile);
            if (clientRecordData.isTimeout()) {
                /*create clientFile at the first read*/
                writeClientTo(clientFile, new ClientRecordData(additional));

            } else if (!clientRecordData.isMe(additional)) {
                sendObject(key, additional.getClientId(), KafkaErrorCode.PROJECT_EDITING_BY_ANOTHER.getCode());
                log.warn("Project editing by another: {}", clientRecordData);
                return;
            }

        } else {
            /*create clientFile at the first read*/
            writeClientTo(clientFile, new ClientRecordData(additional));
        }

        /*create Data message*/
        RecordData recordValue = readFrom(file);

        /*send Header message and then Data message*/
        sendObject(key, additional.getClientId(), 0);
        sendObject(key, recordValue);
    }

    private String copyTemplateToNewProject(RecordAttributes additional) {

        /*TODO: read Project List, create new ProjectID (ProjectList will updated by TWcmd)*/

        /*TODO: read Template Project (file by file)*/

        /*TODO: send message to TWcmd to write new project*/

        return "P2";
    }

    private boolean isTemplate(String projectId) {
        return projectId.startsWith("T");
    }

    private void sendObject(String key, long clientId, long statusCode) {
        dataProducer.send(new ProducerRecord<String, Object>(topic, key, SerializeUtil.serializeHeader(clientId, statusCode)));
    }

    private void sendObject(String key, Object object) {
        dataProducer.send(new ProducerRecord<String, Object>(topic, key, object));
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

        log.info("readFrom( file: {} ):kafkafRecordValue.additional = {}", file, recordData.getAdditional());
        return recordData;
    }

    private ClientRecordData readClientFrom(File file) throws IOException, ClassNotFoundException, InstantiationException {

        ClientRecordData clientRecordData = null;
        FileInputStream fileIn = new FileInputStream(file);

        /*-- normal cast to known object --*/
        ObjectInputStream in = createObjectInputStream(kafkaEnvironmentConfigs.getInputStream(), fileIn);
        ReadSerialize readSerialize = (ReadSerialize) in;
        clientRecordData = (ClientRecordData) readSerialize.readSerialize();
        in.close();
        fileIn.close();

        log.info("readClientFrom( file: {} ):clientRecord = {}", file, clientRecordData);
        return clientRecordData;
    }

    /**
     * IMPORTANT: replace only.
     */
    private void writeClientTo(File file, ClientRecordData clientRecordData) throws IOException, InstantiationException {
        log.info("writeClientTo( file: {}, clientRecord: {} )", file, clientRecordData);
        FileUtil.autoCreateParentDir(file);
        FileOutputStream fileOut = new FileOutputStream(file, false);
        ObjectOutputStream objectOutputStream = createObjectOutputStream(kafkaEnvironmentConfigs.getOutputStream(), fileOut);
        ((WriteSerialize) objectOutputStream).writeSerialize(clientRecordData);
        objectOutputStream.close();
        fileOut.close();
    }

    private File getFile(ProjectFileType projectFileType, RecordAttributes additional) {
        /*TODO: need project data root path from configuration*/
        return getFile(projectFileType, additional, rootPath, "");
    }

    private File getFile(ProjectFileType projectFileType, RecordAttributes additional, String rootPath, String postFix) {
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

    private File getClientFile(RecordAttributes additional) {
        return new File(rootPath + "/" + additional.getProjectId() + "/client");
    }

    private String getFileName(String prefix, String recordId) {
        if (prefix.endsWith("list"))
            return prefix;
        return prefix + recordId;
    }

    /**
     * validate Additional Data and KafkaKey
     **/
    private ProjectFileType validate(String kafkaRecordKey, RecordAttributes additional) throws UnsupportedOperationException, InvalidParameterException {

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
