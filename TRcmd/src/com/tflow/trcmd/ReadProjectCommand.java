package com.tflow.trcmd;

import com.tflow.kafka.*;
import com.tflow.model.data.*;
import com.tflow.model.data.record.ClientRecordData;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.DataMapper;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.wcmd.IOCommand;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Kafka-Topic & Kafka-Key: spec in \TFlow\documents\Data Structure - Kafka.md
 * Kafka-Value: serialized data with additional information or Message Record Value Structure spec in \TFlow\documents\Data Structure - Kafka.md
 */
public class ReadProjectCommand extends IOCommand {

    private Logger log = LoggerFactory.getLogger(ReadProjectCommand.class);

    private String topic;
    private KafkaProducer<String, Object> dataProducer;
    private RecordMapper mapper;
    private HeaderData headerData;
    private DataManager dataManager;

    public ReadProjectCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs, KafkaProducer<String, Object> dataProducer, String topic, DataManager dataManager) {
        super(offset, key, value, environmentConfigs);
        this.dataProducer = dataProducer;
        this.topic = topic;
        this.dataManager = dataManager;
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, InvalidParameterException, IOException, ClassNotFoundException, InstantiationException {
        mapper = Mappers.getMapper(RecordMapper.class);
        RecordAttributesData additional = mapper.map((KafkaRecordAttributes) value);
        headerData = getHeaderData(additional);

        ProjectFileType projectFileType;
        try {
            projectFileType = validate(key, additional);
        } catch (InvalidParameterException ex) {
            KafkaErrorCode kafkaErrorCode = KafkaErrorCode.valueOf(ex.getMessage());
            log.warn("Invalid parameter: {}", kafkaErrorCode);
            headerData.setResponseCode(kafkaErrorCode.getCode());
            sendObject(key, headerData);
            return;
        } catch (UnsupportedOperationException ex) {
            log.warn(ex.getMessage());
            headerData.setResponseCode(KafkaErrorCode.UNSUPPORTED_FILE_TYPE.getCode());
            sendObject(key, headerData);
            return;
        }

        if (ProjectFileType.PROJECT.equals(projectFileType)) {
            /* support open new project from template/existing-project.
             * TEMPLATE_ID/PROJECT_ID:
             * empty string = Add New Empty Project
             * templateID = Copy Template to New Project
             * templatePrefix + projectID = Copy Project to New Project
             */
            String templateId = additional.getProjectId();
            if (templateId.isEmpty()) {
                /*return EmptyProject with new ProjectID*/
                log.warn("Project template not found: {}", additional.getProjectId());
                ProjectData emptyProject = new ProjectData();
                emptyProject.setName("Untitled");

                /*send Header message and then Data message*/
                try {
                    RecordData returnValue = createNewProject(emptyProject, additional);
                    sendObject(key, headerData);
                    sendObject(key, returnValue);
                } catch (Exception ex) {
                    log.error("INVALID_DATA_FILE: ", ex);
                    headerData.setResponseCode(KafkaErrorCode.INVALID_DATA_FILE.getCode());
                    sendObject(key, headerData);
                }
                return;

            } else if (templateId.startsWith(IDPrefix.TEMPLATE.getPrefix())) {
                /*return CopiedProject with new ProjectID*/
                try {
                    String prototypeId = getPrototypeId(templateId);
                    log.debug("prototypeId = {}", prototypeId);
                    ProjectData prototypeProject = getPrototypeProject(prototypeId, additional);
                    RecordData returnValue = createNewProject(prototypeProject, additional);

                    copyProject(prototypeId, prototypeProject.getId(), additional);

                    /*send Header message and then Data message*/
                    sendObject(key, headerData);
                    sendObject(key, returnValue);
                } catch (Exception ex) {
                    log.error("INVALID_DATA_FILE: ", ex);
                    headerData.setResponseCode(KafkaErrorCode.INVALID_DATA_FILE.getCode());
                    sendObject(key, headerData);
                }
                return;
            }

            /* otherwise load record normally */
        }

        File file = getFile(projectFileType, additional);
        if (!file.exists()) {
            headerData.setResponseCode(KafkaErrorCode.DATA_FILE_NOT_FOUND.getCode());
            sendObject(key, headerData);
            log.warn("File not found: {}", file.getAbsolutePath());
            return;
        }

        /*create Data message*/
        RecordData recordValue = (RecordData) readFrom(file);

        /*send Header message and then Data message*/
        sendObject(key, headerData);
        sendObject(key, recordValue);
    }

    /**
     * IMPORTANT: all write in this function must call DataManager.addData to make the TWcmd can replay for Backup Site
     **/
    private void copyProject(String srcProjectId, String destProjectId, RecordAttributesData additional) throws InstantiationException, IOException, ClassNotFoundException {
        log.info("copyProject(from:{}, to:{})", srcProjectId, destProjectId);
        String projectId = additional.getProjectId();

        additional.setProjectId(srcProjectId);
        File projectFile = getFile(ProjectFileType.PROJECT, additional);
        File srcProjectDir = projectFile.getParentFile();

        /*collect directory to list*/
        List<File> dirList = new ArrayList<>(Collections.singletonList(srcProjectDir));
        int index = 0;
        while (index < dirList.size()) {
            File onDir = dirList.get(index++);
            File[] subDir = onDir.listFiles(dir -> !dir.isFile());
            if (subDir != null) {
                dirList.addAll(Arrays.asList(subDir));
            }
        }

        /*add files to Destination-Project using DataManager */
        RecordData recordData;
        RecordAttributesData recordAttributes;
        ProjectFileType projectFileType;
        KafkaRecordAttributes kafkaRecordAttributes;
        File[] files;
        for (File dir : dirList) {
            log.debug("copyProject.dir:{}", dir);

            files = dir.listFiles(File::isFile);
            if (files == null) continue;

            for (File file : files) {
                log.debug("copyProject.file:{}", file);

                recordData = (RecordData) readFrom(file);
                recordAttributes = recordData.getAdditional();
                projectFileType = recordAttributes.getFileType();
                kafkaRecordAttributes = mapper.map(recordAttributes);
                kafkaRecordAttributes.setProjectId(destProjectId);
                Object data = recordData.getData();

                // change all projectId in projectFile
                if (projectFileType == ProjectFileType.PROJECT) {
                    ProjectData projectData = (ProjectData) data;
                    projectData.setId(destProjectId);
                    kafkaRecordAttributes.setRecordId(destProjectId);
                }

                dataManager.addData(projectFileType, data, kafkaRecordAttributes);
            }

            dataManager.waitAllTasks();
        }
    }

    private ProjectData getPrototypeProject(String prototypeId, RecordAttributesData additional) throws InstantiationException, IOException, ClassNotFoundException {
        String projectId = additional.getProjectId();
        String recordId = additional.getRecordId();
        additional.setProjectId(prototypeId);
        additional.setRecordId(prototypeId);

        File file = getFile(ProjectFileType.PROJECT, additional);
        if (!file.exists()) {
            throw new IOException("Prototype-Project(" + prototypeId + ") not found!");
        }
        RecordData recordValue = (RecordData) readFrom(file);

        additional.setProjectId(projectId);
        additional.setRecordId(recordId);
        return (ProjectData) recordValue.getData();
    }

    private String getPrototypeId(String templateId) {
        String projectId = templateId.substring(1);
        log.debug("getPrototypeId(templateId:{}): projectId = {}", templateId, projectId);
        if (projectId.startsWith(IDPrefix.PROJECT.getPrefix())) {
            return projectId;
        }
        return templateId;
    }

    private HeaderData getHeaderData(RecordAttributesData additional) {
        HeaderData headerData = new HeaderData();
        headerData.setProjectId(additional.getProjectId());
        headerData.setUserId(additional.getModifiedUserId());
        headerData.setClientId(additional.getModifiedClientId());
        headerData.setTime(additional.getModifiedDate().getTime());
        headerData.setTransactionId(additional.getTransactionId());
        return headerData;
    }

    /**
     * IMPORTANT: all write in this function must call DataManager.addData to make the TWcmd can replay for Backup Site
     *
     * @param prototypeData the prototypeData.Id will replaced by new projectId
     * @return new RecordData with the data = prototypeData
     */
    private RecordData createNewProject(ProjectData prototypeData, RecordAttributesData additional) throws InstantiationException, IOException, ClassNotFoundException {

        GroupListData groupList;
        File file = getFile(ProjectFileType.GROUP_LIST, additional);
        if (!file.exists()) {
            /*first time access to GROUP_LIST*/
            groupList = new GroupListData();
            groupList.setGroupList(new ArrayList<>());
        } else {
            RecordData recordData = (RecordData) readFrom(file);
            groupList = (GroupListData) recordData.getData();
        }

        DataMapper mapper = Mappers.getMapper(DataMapper.class);
        GroupData groupData;
        File groupFile = getFile(ProjectFileType.GROUP, additional);
        if (!groupFile.exists()) {
            groupData = new GroupData();
            groupData.setId(Integer.parseInt(additional.getRecordId()));
            groupData.setName("Ungrouped");
            groupData.setProjectList(new ArrayList<>());
            groupList.getGroupList().add(mapper.map(groupData));
        } else {
            RecordData recordData = (RecordData) readFrom(groupFile);
            groupData = (GroupData) recordData.getData();
        }

        int newProjectId = groupList.getLastProjectId() + 1;
        String newProjectIdString = IDPrefix.PROJECT.getPrefix() + newProjectId;
        prototypeData.setId(newProjectIdString);
        log.info("new project id = {}", newProjectIdString);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setId(additional.getProjectId());
        projectUser.setUserId(additional.getModifiedUserId());
        projectUser.setClientId(additional.getModifiedClientId());

        groupList.setLastProjectId(newProjectId);
        dataManager.addData(ProjectFileType.GROUP_LIST, groupList, projectUser);

        groupData.getProjectList().add(mapper.map(prototypeData));
        dataManager.addData(ProjectFileType.GROUP, groupData, projectUser, groupData.getId());

        /*create empty project object in a recordData*/
        RecordData recordData = new RecordData();
        recordData.setData(prototypeData);
        recordData.setAdditional(additional);
        additional.setRecordId(additional.getProjectId());
        return recordData;
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

    private String copyTemplateToNewProject(RecordAttributesData additional) {
        log.info("New project from template({})", additional.getProjectId());
        /*TODO: read Project List, create new ProjectID (ProjectList will updated by TWcmd)*/

        /*TODO: read Template Project (file by file)*/

        /*TODO: send message to TWcmd to write new project*/

        return null;
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
        if (requireType > 0 && additional.getProjectId() == null) {
            throw new InvalidParameterException(KafkaErrorCode.REQUIRES_PROJECT_ID.name());
        }

        // stepId is required on all types except type(1).
        if (requireType > 1 && requireType < 9 && additional.getStepId() == null) {
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

    @Override
    public String toString() {
        return headerData == null ? super.toString() : headerData.toString();
    }
}
