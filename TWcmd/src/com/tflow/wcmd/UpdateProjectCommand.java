package com.tflow.wcmd;

import com.tflow.kafka.KafkaRecordValue;
import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.kafka.ProjectFileType;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Kafka-Topic: UpdateProject
 * Kafka-Key: shorten record key spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class UpdateProjectCommand extends WriteCommand {

    private Logger log = LoggerFactory.getLogger(UpdateProjectCommand.class);

    public UpdateProjectCommand(ConsumerRecord<String, String> kafkaRecord) {
        super(kafkaRecord);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException {
        KafkaRecordValue kafkaRecordValue = (KafkaRecordValue) SerializeUtil.deserialize(kafkaRecord.value());
        ProjectFileType projectFileType = ProjectFileType.parse(kafkaRecord.key());
        validate(projectFileType, kafkaRecordValue);

        KafkaTWAdditional additional = (KafkaTWAdditional) kafkaRecordValue.getAdditional();
        Date now = DateTimeUtil.now();
        additional.setModifiedDate(now);

        File file = getFile(projectFileType, additional);
        if (file.exists()) {

            /*move existing Data File to Transaction folder*/
            File historyFile = getHistoryFile(projectFileType, additional);
            String fullFileName = file.toString();
            if (!file.renameTo(historyFile)) {
                log.error("Unexpected case: failed to rename projectfile({}) to historyFile({})!", file, historyFile);
            }

            /*need created-info from history*/
            KafkaRecordValue historyRecord = readFrom(file);
            KafkaTWAdditional historyAdditional = (KafkaTWAdditional) historyRecord.getAdditional();
            additional.setCreatedDate(historyAdditional.getCreatedDate());
            additional.setCreatedUserId(historyAdditional.getCreatedUserId());
            additional.setCreatedClientId(historyAdditional.getCreatedClientId());

            file = new File(fullFileName);

        } else {
            additional.setCreatedDate(now);
            additional.setCreatedUserId(additional.getModifiedUserId());
            additional.setCreatedClientId(additional.getModifiedClientId());
        }

        writeTo(file, kafkaRecordValue);
    }

    private KafkaRecordValue readFrom(File file) throws IOException, ClassNotFoundException {

        KafkaRecordValue kafkaRecordValue = null;
        FileInputStream fileIn = new FileInputStream(file);

        /*-- normal cast to known object --*/
        ObjectInputStream in = new ObjectInputStream(fileIn);
        kafkaRecordValue = (KafkaRecordValue) in.readObject();
        in.close();
        fileIn.close();

        log.info("readFrom( file: {} ).kafkaRecordValue = {}", file, kafkaRecordValue);
        return kafkaRecordValue;
    }

    private void writeTo(File file, KafkaRecordValue kafkaRecordValue) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOut);
        objectOutputStream.writeObject(kafkaRecordValue);
        objectOutputStream.close();
        fileOut.close();
    }

    public void testWriteSerialized(byte[] serialized) {
        try {
            FileOutputStream fileOut = new FileOutputStream("/Apps/TFlow/TestConsumerSerialize.ser");
            fileOut.write(serialized);
            fileOut.close();
            log.info("testWriteSerialized: Serialized data is saved in /Apps/TFlow/TestConsumerSerialize.ser");
        } catch (IOException i) {
            log.error("testWriteSerialized failed,", i);
        }
    }


    private File getHistoryFile(ProjectFileType projectFileType, KafkaTWAdditional additional) {
        /*TODO: need history root path from configuration*/
        String rootPath = "C:/Apps/TFlow/hist";
        return getFile(projectFileType, additional, rootPath, DateTimeUtil.getStr(additional.getModifiedDate(), "-yyyyddMMHHmmssSSS"));
    }

    private File getFile(ProjectFileType projectFileType, KafkaTWAdditional additional) {
        /*TODO: need project data root path from configuration*/
        String rootPath = "C:/Apps/TFlow/project";
        return getFile(projectFileType, additional, rootPath, "");
    }

    private File getFile(ProjectFileType projectFileType, KafkaTWAdditional additional, String rootPath, String postFix) {
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

        return new File(rootPath + path + "/" + projectFileType.getKey());
    }

    private void validate(ProjectFileType projectFileType, KafkaRecordValue kafkaRecordValue) throws UnsupportedOperationException, InvalidParameterException {
        /*validate Additional Data and KafkaKey*/

        if (projectFileType == null) {
            throw new UnsupportedOperationException("Invalid project-file-type '" + projectFileType.getKey() + "'!");
        }

        /*check required data for the KafkaKey*/
        KafkaTWAdditional additional = (KafkaTWAdditional) kafkaRecordValue.getAdditional();
        int requireType = projectFileType.getRequireType();

        // projectId is required on all types.
        if (additional.getProjectId() == null) {
            throw new InvalidParameterException("Additional.ProjectId is required for project-file-type('" + projectFileType.getKey() + "')");
        }

        // stepId is required on all types except type(1).
        if (requireType > 1 && additional.getStepId() == null) {
            throw new InvalidParameterException("Additional.StepId is required for project-file-type('" + projectFileType.getKey() + "')");
        }

        // dataTableId is required on type 3 only.
        if (requireType == 3 && additional.getDataTableId() == null) {
            throw new InvalidParameterException("Additional.DataTableId is required for project-file-type('" + projectFileType.getKey() + "')");
        }

        // transformTableId is required on type 4 only.
        if (requireType == 4 && additional.getTransformTableId() == null) {
            throw new InvalidParameterException("Additional.TransformTableId is required for project-file-type('" + projectFileType.getKey() + "')");
        }

    }


}
