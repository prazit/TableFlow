package com.tflow.trcmd;

import com.tflow.kafka.KafkaRecordValue;
import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.kafka.ProjectFileType;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import com.tflow.wcmd.KafkaCommand;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidParameterException;
import java.util.Date;

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class ReadProjectCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(ReadProjectCommand.class);

    private KafkaProducer producer;

    public ReadProjectCommand(ConsumerRecord<String, String> kafkaRecord, KafkaProducer producer) {
        super(kafkaRecord);
        this.producer = producer;
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException {
        KafkaRecordValue kafkaRecordValue = (KafkaRecordValue) SerializeUtil.deserialize(kafkaRecord.value());
        ProjectFileType projectFileType = validate(kafkaRecord.key(), kafkaRecordValue);

        KafkaTWAdditional additional = (KafkaTWAdditional) kafkaRecordValue.getAdditional();
        Date now = DateTimeUtil.now();
        additional.setModifiedDate(now);

        File file = getFile(projectFileType, additional);
        if (!file.exists()) {
            /*TODO: create Header message for error*/

            /*TODO: send Header message*/
             return;
        }

        /*TODO: create Header message*/

        /*TODO: create Data message*/

        /*TODO: send Header message*/

        /*TODO: send Data message*/
    }

    private KafkaRecordValue readFrom(File file) throws IOException, ClassNotFoundException {

        KafkaRecordValue kafkaRecordValue = null;
        FileInputStream fileIn = new FileInputStream(file);

        /*-- normal cast to known object --*/
        ObjectInputStream in = new ObjectInputStream(fileIn);
        kafkaRecordValue = (KafkaRecordValue) in.readObject();
        in.close();
        fileIn.close();

        log.info("readFrom( file: {} ). kafkafRecordValue.additional = {}", file, kafkaRecordValue.getAdditional());
        return kafkaRecordValue;
    }

    private File getFile(ProjectFileType projectFileType, KafkaTWAdditional additional) {
        /*TODO: need project data root path from configuration*/
        String rootPath = "/Apps/TFlow/project";
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
    private ProjectFileType validate(String kafkaRecordKey, KafkaRecordValue kafkaRecordValue) throws UnsupportedOperationException, InvalidParameterException {

        ProjectFileType fileType;
        try {
            fileType = ProjectFileType.valueOf(kafkaRecordKey);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Invalid operation '" + kafkaRecordKey + "', recommends to use value from enum 'ProjectFileType' !!");
        }

        /*check required data for the KafkaKey*/
        KafkaTWAdditional additional = (KafkaTWAdditional) kafkaRecordValue.getAdditional();
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

        return fileType;
    }


}
