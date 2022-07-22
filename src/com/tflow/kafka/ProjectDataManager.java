package com.tflow.kafka;

import com.google.gson.internal.LinkedTreeMap;
import com.tflow.model.data.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.*;
import com.tflow.system.Environment;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.*;
import java.util.Properties;

/*TODO: save updated object between line in RemoveDataFile when the Action RemoveDataFile is used in the UI*/
public class ProjectDataManager {

    private Logger log = LoggerFactory.getLogger(ProjectDataManager.class);

    private List<ProjectDataWriteBuffer> projectDataWriteBufferList = new ArrayList<>();

    EnvironmentConfigs environmentConfigs;
    private String writeTopic;
    private String readTopic;
    private String dataTopic;
    private long commitAgainMilliseconds;
    private boolean commitWaiting;
    private Producer<String, Object> producer;
    private Consumer<String, byte[]> consumer;

    private Deserializer deserializer;

    /*TODO: remove Collection below, it for test*/
    public List<ProjectDataWriteBuffer> testBuffer = new ArrayList<>();

    public ProjectMapper mapper;

    public ProjectDataManager(Environment environment) {
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
        createMappers();
    }

    private void createMappers() {
        mapper = Mappers.getMapper(ProjectMapper.class);
    }

    private boolean createProducer() {
        /* Notice: some properties from: https://www.tutorialspoint.com/apache_kafka/apache_kafka_simple_producer_example.htm
         * All properties please see the log message when the producer is loaded (already shown below).
            INFO  [org.apache.kafka.clients.producer.ProducerConfig] (default task-10) ProducerConfig values:
                acks = -1
                batch.size = 16384
                bootstrap.servers = [DESKTOP-K1PAMA3:9092]
                buffer.memory = 33554432
                client.dns.lookup = use_all_dns_ips
                client.id = producer-1
                compression.type = none
                connections.max.idle.ms = 540000
                delivery.timeout.ms = 120000
                enable.idempotence = true
                interceptor.classes = []
                key.serializer = class org.apache.kafka.common.serialization.StringSerializer
                linger.ms = 1
                max.block.ms = 60000
                max.in.flight.requests.per.connection = 5
                max.request.size = 1048576
                metadata.max.age.ms = 300000
                metadata.max.idle.ms = 300000
                metric.reporters = []
                metrics.num.samples = 2
                metrics.recording.level = INFO
                metrics.sample.window.ms = 30000
                partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
                receive.buffer.bytes = 32768
                reconnect.backoff.max.ms = 1000
                reconnect.backoff.ms = 50
                request.timeout.ms = 30000
                retries = 0
                retry.backoff.ms = 100
                sasl.client.callback.handler.class = null
                sasl.jaas.config = null
                sasl.kerberos.kinit.cmd = /usr/bin/kinit
                sasl.kerberos.min.time.before.relogin = 60000
                sasl.kerberos.service.name = null
                sasl.kerberos.ticket.renew.jitter = 0.05
                sasl.kerberos.ticket.renew.window.factor = 0.8
                sasl.login.callback.handler.class = null
                sasl.login.class = null
                sasl.login.connect.timeout.ms = null
                sasl.login.read.timeout.ms = null
                sasl.login.refresh.buffer.seconds = 300
                sasl.login.refresh.min.period.seconds = 60
                sasl.login.refresh.window.factor = 0.8
                sasl.login.refresh.window.jitter = 0.05
                sasl.login.retry.backoff.max.ms = 10000
                sasl.login.retry.backoff.ms = 100
                sasl.mechanism = GSSAPI
                sasl.oauthbearer.clock.skew.seconds = 30
                sasl.oauthbearer.expected.audience = null
                sasl.oauthbearer.expected.issuer = null
                sasl.oauthbearer.jwks.endpoint.refresh.ms = 3600000
                sasl.oauthbearer.jwks.endpoint.retry.backoff.max.ms = 10000
                sasl.oauthbearer.jwks.endpoint.retry.backoff.ms = 100
                sasl.oauthbearer.jwks.endpoint.url = null
                sasl.oauthbearer.scope.claim.name = scope
                sasl.oauthbearer.sub.claim.name = sub
                sasl.oauthbearer.token.endpoint.url = null
                security.protocol = PLAINTEXT
                security.providers = null
                send.buffer.bytes = 131072
                socket.connection.setup.timeout.max.ms = 30000
                socket.connection.setup.timeout.ms = 10000
                ssl.cipher.suites = null
                ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
                ssl.endpoint.identification.algorithm = https
                ssl.engine.factory.class = null
                ssl.key.password = null
                ssl.keymanager.algorithm = SunX509
                ssl.keystore.certificate.chain = null
                ssl.keystore.key = null
                ssl.keystore.location = null
                ssl.keystore.password = null
                ssl.keystore.type = JKS
                ssl.protocol = TLSv1.3
                ssl.provider = null
                ssl.secure.random.implementation = null
                ssl.trustmanager.algorithm = PKIX
                ssl.truststore.certificates = null
                ssl.truststore.location = null
                ssl.truststore.password = null
                ssl.truststore.type = JKS
                transaction.timeout.ms = 60000
                transactional.id = null
                value.serializer = class org.apache.kafka.common.serialization.StringSerializer
            10:37:52,212 INFO  [org.apache.kafka.common.utils.AppInfoParser] (default task-10) Kafka version: 3.1.0
        **/
        /*TODO: need to load producer configuration*/
        writeTopic = "project-write";
        readTopic = "project-read";
        dataTopic = "project-data";
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("request.timeout.ms", 30000);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.encoding", "UTF-8");
        props.put("value.serializer", environmentConfigs.getKafkaSerializer());
        producer = new KafkaProducer<>(props);

        return true;
    }

    private boolean createConsumer(long clientId) {
        Properties props = new Properties();

        /*TODO: need to load consumer configuration*/
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.interval.ms", "30000");
        props.put("max.poll.records", "2");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        props.put("group.id", "tflow" + clientId);

        subscribeTo(dataTopic, consumer = new KafkaConsumer<>(props));

        try {
            deserializer = SerializeUtil.getDeserializer(environmentConfigs.getKafkaDeserializer());
        } catch (Exception ex) {
            log.error("Deserializer creation failed: ", ex);
            return false;
        }

        return true;
    }

    private void subscribeTo(String topic, Consumer consumer) {
        List<PartitionInfo> topicPartitionList = consumer.partitionsFor(topic);
        ArrayList<TopicPartition> arrTopic = new ArrayList<>();

        for (PartitionInfo partitionInfo : topicPartitionList) {
            arrTopic.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }

        //need to use assign instead of subscribe: consumer.subscribe(Collections.singletonList(topic));
        consumer.assign(arrTopic);
        log.info("consumer created and subscribed to topic({})", dataTopic);

        consumer.poll(Duration.ofMillis(0));
        consumer.seekToEnd(arrTopic);

        for (TopicPartition topicPartition : arrTopic) {
            log.info("partition:{}, position:{}", topicPartition, consumer.position(topicPartition) - 1);
        }
    }

    private boolean ready(Producer<String, Object> producer) {
        if (producer == null) {
            if (!createProducer()) {
                return false;
            }
        }

        /*TODO: check status of Producer(kafka server)*/
        // need log.warn("something about server status");

        return true;
    }

    private boolean readyToCapture(Consumer<String, byte[]> consumer, long clientId) {
        if (consumer == null && !createConsumer(clientId)) {
            return false;
        }

        /*TODO: check status of Consumer(kafka server)*/
        // need log.warn("something about server status");

        return true;
    }

    /**
     * Wait milliseconds and then commit.
     */
    private void commit(long milliseconds) {
        commitWaiting = true;

        /*TODO: Wait milliseconds, please use Scheduler.*/
        /*TODO: after wait, need to set commitWaiting = false; and then commit*/
    }

    /**
     * TODO: need to run ProjectWriteCommand in another thread, this process guarantee success no need to wait for it.
     * TODO: need to update Client-Data-file before execute first command
     */
    private void commit() {
        if (commitWaiting) return;

        ArrayList<ProjectDataWriteBuffer> commitList = new ArrayList<>(projectDataWriteBufferList);
        KafkaRecordAttributes additional;
        ProjectFileType fileType;
        String recordId;
        KafkaRecord kafkaRecord;
        String key;
        String value;
        for (ProjectDataWriteBuffer writeCommand : commitList) {
            if (!ready(producer)) {
                commit(commitAgainMilliseconds);
                return;
            }

            additional = writeCommand.getAdditional();
            fileType = writeCommand.getFileType();
            recordId = additional.getRecordId();
            key = fileType.name();
            kafkaRecord = new KafkaRecord(writeCommand.getDataObject(), additional);
            log.info("ProjectWriteCommand( fileType:{}, recordId:{} ) started.", fileType.name(), recordId);

            producer.send(new ProducerRecord<>(writeTopic, key, kafkaRecord));
            projectDataWriteBufferList.remove(writeCommand);
            testBuffer.add(writeCommand);

            log.info("ProjectWriteCommand completed.");
        }
    }

    public void addData(ProjectFileType fileType, List<Integer> idList, Project project) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId());
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId());
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, Project project, String recordId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), recordId);
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project, String recordId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), recordId);
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project, int recordId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, Project project, int recordId, int stepId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project, int recordId, int stepId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, Project project, int recordId, int stepId, int dataTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project, int recordId, int stepId, int dataTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, Project project, int recordId, int stepId, int ignoredId, int transformTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, Project project, int recordId, int stepId, int ignoredId, int transformTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, (Object) object, additional);
    }

    private void addData(ProjectFileType fileType, TWData object, KafkaRecordAttributes additional) throws InvalidParameterException {
        addData(fileType, (Object) object, additional);
    }

    private void addData(ProjectFileType fileType, Object object, KafkaRecordAttributes additional) throws InvalidParameterException {
        if (additional.getUserId() <= 0) throw new InvalidParameterException("Required Field: ModifiedUserId for ProjectDataManager.addData(" + fileType + ")");
        if (additional.getClientId() <= 0) throw new InvalidParameterException("Required Field: ModifiedClientId for ProjectDataManager.addData(" + fileType + ")");

        // Notice: all of below copied from class com.flow.wcmd.UpdateProjectCommand.validate(String kafkaRecordKey, KafkaRecordValue kafkaRecordValue)
        int requireType = fileType.getRequireType();
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) throw new InvalidParameterException("Required Field: RecordId for ProjectDataManager.addData(" + fileType + ")");
        if (additional.getProjectId() == null) throw new InvalidParameterException("Required Field: ProjectId for ProjectDataManager.addData(" + fileType + ")");
        if (requireType > 1 && additional.getStepId() == null) throw new InvalidParameterException("Required Field: StepId for ProjectDataManager.addData(" + fileType + ")");
        if (requireType == 3 && additional.getDataTableId() == null) throw new InvalidParameterException("Required Field: DataTableId for ProjectDataManager.addData(" + fileType + ")");
        if (requireType == 4 && additional.getTransformTableId() == null) throw new InvalidParameterException("Required Field: TransformTableId for ProjectDataManager.addData(" + fileType + ")");

        projectDataWriteBufferList.add(new ProjectDataWriteBuffer(fileType, object, additional));
        commit();
    }

    public void addProjectAs(String newProjectId, Project project) {
        String oldId = project.getId();
        project.setId(newProjectId);
        ProjectData projectData = mapper.map(project);

        addData(ProjectFileType.PROJECT, projectData, project, newProjectId);

        Map<Integer, Database> databaseMap = project.getDatabaseMap();
        addData(ProjectFileType.DB_LIST, mapper.fromMap(databaseMap), project, "1");
        for (Database database : databaseMap.values()) {
            addData(ProjectFileType.DB, mapper.map(database), project, database.getId());
        }

        Map<Integer, SFTP> sftpMap = project.getSftpMap();
        addData(ProjectFileType.SFTP_LIST, mapper.fromMap(sftpMap), project, "2");
        for (SFTP sftp : sftpMap.values()) {
            addData(ProjectFileType.SFTP, mapper.map(sftp), project, sftp.getId());
        }

        Map<Integer, Local> localMap = project.getLocalMap();
        addData(ProjectFileType.LOCAL_LIST, mapper.fromMap(localMap), project, "3");
        for (Local local : localMap.values()) {
            addData(ProjectFileType.LOCAL, mapper.map(local), project, local.getId());
        }

        Map<String, Variable> variableMap = project.getVariableMap();
        addData(ProjectFileType.VARIABLE_LIST, mapper.fromVarMap(variableMap), project, "3");
        for (Variable variable : variableMap.values()) {
            addData(ProjectFileType.VARIABLE, mapper.map(variable), project, variable.getName());
        }

        addData(ProjectFileType.STEP_LIST, mapper.fromStepList(project.getStepList()), project, "4");
        for (Step step : project.getStepList()) {
            addStep(step, project);
        }

        project.setId(oldId);
    }

    public void addStep(Step step, Project project) {
        int stepId = step.getId();
        addData(ProjectFileType.STEP, mapper.map(step), project, stepId, stepId);

        /*add each tower in step*/
        List<Tower> towerList = Arrays.asList(step.getDataTower(), step.getTransformTower(), step.getOutputTower());
        for (Tower tower : towerList) {
            addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), stepId);
        }

        /*add data-source-selector-list*/
        List<DataSourceSelector> dataSourceSelectorList = step.getDataSourceSelectorList();
        addData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, mapper.fromDataSourceSelectorList(dataSourceSelectorList), project, 0, stepId);

        /*add data-source-selector*/
        for (DataSourceSelector dataSourceSelector : dataSourceSelectorList) {
            addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map(dataSourceSelector), project, dataSourceSelector.getId(), stepId);
        }

        /*add data-file-list*/
        List<DataFile> dataFileList = step.getFileList();
        addData(ProjectFileType.DATA_FILE_LIST, mapper.fromDataFileList(dataFileList), project, 0, stepId);

        /*add data-file*/
        for (DataFile dataFile : dataFileList) {
            addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), project, dataFile.getId(), stepId);
        }

        /*add data-table-list*/
        List<DataTable> dataList = step.getDataList();
        addData(ProjectFileType.DATA_TABLE_LIST, mapper.fromDataTableList(dataList), project, 1, stepId);

        /*add each data-table in data-table-list*/
        for (DataTable dataTable : dataList) {
            int dataTableId = dataTable.getId();

            /*add data-table*/
            addData(ProjectFileType.DATA_TABLE, mapper.map(dataTable), project, dataTableId, stepId, dataTableId);

            /*add column-list*/
            List<DataColumn> columnList = dataTable.getColumnList();
            addData(ProjectFileType.DATA_COLUMN_LIST, mapper.fromDataColumnList(columnList), project, 2, stepId, dataTableId);

            /*add each column in column-list*/
            for (DataColumn dataColumn : columnList) {
                addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), project, dataColumn.getId(), stepId, dataTableId);
            }

            /*add output-list*/
            List<OutputFile> outputList = dataTable.getOutputList();
            addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, 2, stepId, dataTableId);

            /*add each output in output-list*/
            for (OutputFile output : outputList) {
                addData(ProjectFileType.DATA_OUTPUT, mapper.map(output), project, output.getId(), stepId, dataTableId);
            }
        }

        /*add transform-table-list*/
        List<TransformTable> transformList = step.getTransformList();
        addData(ProjectFileType.TRANSFORM_TABLE_LIST, mapper.fromTransformTableList(transformList), project, 4, stepId);

        /*add each transform-table in transform-table-list*/
        for (TransformTable transformTable : transformList) {
            int transformTableId = transformTable.getId();

            /*add Transform-table*/
            addData(ProjectFileType.TRANSFORM_TABLE, mapper.map(transformTable), project, transformTableId, stepId, 0, transformTableId);

            /*add transform-column-list*/
            List<DataColumn> columnList = transformTable.getColumnList();
            addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), project, 5, stepId, 0, transformTableId);

            /*add each transform-column in transform-column-list*/
            for (DataColumn dataColumn : columnList) {
                addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) dataColumn), project, dataColumn.getId(), stepId, 0, transformTableId);
            }

            /*add each transform-columnfx in transform-table(columnFxTable)*/
            for (ColumnFx columnFx : transformTable.getColumnFxTable().getColumnFxList()) {
                addData(ProjectFileType.TRANSFORM_COLUMNFX, mapper.map(columnFx), project, columnFx.getId(), stepId, 0, transformTableId);
            }

            /*add transform-output-list*/
            List<OutputFile> outputList = transformTable.getOutputList();
            addData(ProjectFileType.TRANSFORM_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, 6, stepId, 0, transformTableId);

            /*add each transform-output in transform-output-list*/
            for (OutputFile output : outputList) {
                addData(ProjectFileType.TRANSFORM_OUTPUT, mapper.map(output), project, output.getId(), stepId, 0, transformTableId);
            }

            /*add tranformation-list*/
            List<TableFx> fxList = transformTable.getFxList();
            addData(ProjectFileType.TRANSFORMATION_LIST, mapper.fromTableFxList(fxList), project, 7, stepId, 0, transformTableId);

            /*add each tranformation in tranformation-list*/
            for (TableFx tableFx : fxList) {
                addData(ProjectFileType.TRANSFORMATION, mapper.map(tableFx), project, tableFx.getId(), stepId, 0, transformTableId);
            }
        }

        /*add line-list at the end*/
        List<Line> lineList = step.getLineList();
        addData(ProjectFileType.LINE_LIST, mapper.fromLineList(lineList), project, 8, stepId);

        /*add each line in line-list*/
        for (Line line : lineList) {
            addData(ProjectFileType.LINE, mapper.map(line), project, line.getId(), stepId);
        }

        // update Step List
        addData(ProjectFileType.STEP_LIST, mapper.fromStepList(project.getStepList()), project, "");

        // update Project data: need to update Project record every Action that call the newUniqueId*/
        addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

    /**
     * TODO: need to support open new project from template (projectId < 0)
     **/
    @SuppressWarnings("unchecked")
    public Project getProject(Workspace workspace) throws ClassCastException, ProjectDataException {
        String projectId = workspace.getProject().getId();
        long clientId = workspace.getClient().getId();
        long userId = workspace.getUser().getId();

        /*get project, to know the project is not edit by another */
        Object data = getData(ProjectFileType.PROJECT, new KafkaRecordAttributes(clientId, userId, projectId, projectId));
        Project project = mapper.map((ProjectData) throwExceptionOnError(data));
        project.setOwer(workspace);
        project.setManager(this);

        /*get db-list*/
        data = getData(ProjectFileType.DB_LIST, project, "1");
        List<Integer> databaseIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, Database> databaseMap = new HashMap<>();
        project.setDatabaseMap(databaseMap);

        /*get each db in db-list*/
        for (Integer id : databaseIdList) {
            data = getData(ProjectFileType.DB, project, String.valueOf(id));
            databaseMap.put(id, mapper.map((DatabaseData) throwExceptionOnError(data)));
        }

        /*get sftp-list*/
        data = getData(ProjectFileType.SFTP_LIST, project, "2");
        List<Integer> sftpIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, SFTP> sftpMap = new HashMap<>();
        project.setSftpMap(sftpMap);

        /*get each sftp in sftp-list*/
        for (Integer id : sftpIdList) {
            data = getData(ProjectFileType.SFTP, project, String.valueOf(id));
            sftpMap.put(id, mapper.map((SFTPData) throwExceptionOnError(data)));
        }

        /*get local-list*/
        data = getData(ProjectFileType.LOCAL_LIST, project, "3");
        List<Integer> localIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, Local> localMap = new HashMap<>();
        project.setLocalMap(localMap);

        /*get each local in local-list*/
        for (Integer id : localIdList) {
            data = getData(ProjectFileType.LOCAL, project, String.valueOf(id));
            localMap.put(id, mapper.map((LocalData) throwExceptionOnError(data)));
        }

        /*get variable-list*/
        data = getData(ProjectFileType.VARIABLE_LIST, project, "4");
        List<String> varIdList = (List<String>) throwExceptionOnError(data);
        Map<String, Variable> varMap = new HashMap<>();
        project.setVariableMap(varMap);

        /*get each variable in variable-list*/
        for (String id : varIdList) {
            data = getData(ProjectFileType.VARIABLE, project, id);
            varMap.put(id, mapper.map((VariableData) throwExceptionOnError(data)));
        }

        /*get step-list*/
        data = getData(ProjectFileType.STEP_LIST, project, "5");
        List<StepItemData> stepItemDataList = mapper.fromLinkedTreeMap((List<LinkedTreeMap>) throwExceptionOnError(data));
        project.setStepList(mapper.toStepList(stepItemDataList));

        workspace.setProject(project);
        return project;
    }

    @SuppressWarnings("unchecked")
    public Step getStep(Project project, int stepIndex) throws Exception {
        List<Step> stepList = project.getStepList();
        Step stepModel = stepList.get(stepIndex);
        int stepId = stepModel.getId();

        /*get step*/
        Object data = getData(ProjectFileType.STEP, project, stepId, stepId);
        StepData stepData = (StepData) throwExceptionOnError(data);
        Step step = mapper.map(stepData);
        step.setOwner(project);

        /*get each tower in step*/
        List<Integer> towerIdList = Arrays.asList(step.getDataTower().getId(), step.getTransformTower().getId(), step.getOutputTower().getId());
        List<Tower> towerList = new ArrayList<>();
        for (Integer towerId : towerIdList) {
            data = getData(ProjectFileType.TOWER, project, towerId, stepId);
            Tower tower = mapper.map((TowerData) throwExceptionOnError(data));
            tower.setOwner(step);
            towerList.add(tower);

            /*create each floor in tower*/
            int roomsOnAFloor = tower.getRoomsOnAFloor();
            List<Floor> floorList = tower.getFloorList();
            for (int floorIndex = 0; floorIndex < floorList.size(); floorIndex++) {
                Floor floor = floorList.get(floorIndex);
                floor.setIndex(floorIndex);
                floor.setTower(tower);

                /*create each room in a floor*/
                List<Room> roomList = floor.getRoomList();
                roomList.clear();
                for (int index = 0; index < roomsOnAFloor; index++) {
                    roomList.add(new EmptyRoom(index, floor));
                }
            }
        }
        step.setDataTower(towerList.get(0));
        step.setTransformTower(towerList.get(1));
        step.setOutputTower(towerList.get(2));

        /*get data-source-selector-list*/
        data = getData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, project, 1, stepId);
        List<Integer> dataSourceSelectorIdList = mapper.fromDoubleList(((List<Double>) throwExceptionOnError(data)));
        List<DataSourceSelector> dataSourceSelectorList = new ArrayList<>();
        step.setDataSourceSelectorList(dataSourceSelectorList);

        /*get data-source-selector*/
        for (Integer dataSourceSelectorId : dataSourceSelectorIdList) {
            data = getData(ProjectFileType.DATA_SOURCE_SELECTOR, project, dataSourceSelectorId, stepId);
            DataSourceSelector dataSourceSelector = mapper.map((DataSourceSelectorData) throwExceptionOnError(data));
            dataSourceSelector.setOwner(step);
            dataSourceSelectorList.add(dataSourceSelector);
            step.getDataTower().setRoom(dataSourceSelector.getFloorIndex(), dataSourceSelector.getRoomIndex(), dataSourceSelector);
            dataSourceSelector.createPlugListeners();
        }

        /*get data-file-list*/
        data = getData(ProjectFileType.DATA_FILE_LIST, project, 1, stepId);
        List<Integer> dataFileIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        List<DataFile> dataFileList = new ArrayList<>();
        step.setFileList(dataFileList);

        /*get data-file*/
        for (Integer dataFileId : dataFileIdList) {
            data = getData(ProjectFileType.DATA_FILE, project, dataFileId, stepId);
            DataFile dataFile = mapper.map((DataFileData) throwExceptionOnError(data));
            dataFileList.add(dataFile);
            step.getDataTower().setRoom(dataFile.getFloorIndex(), dataFile.getRoomIndex(), dataFile);
        }

        /*get data-table-list*/
        data = getData(ProjectFileType.DATA_TABLE_LIST, project, 1, stepId);
        List<Integer> dataTableIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        List<DataTable> dataTableList = new ArrayList<>();
        step.setDataList(dataTableList);

        /*get each data-table in data-table-list*/
        for (Integer dataTableId : dataTableIdList) {
            data = getData(ProjectFileType.DATA_TABLE, project, dataTableId, stepId, dataTableId);
            DataTable dataTable = mapper.map((DataTableData) throwExceptionOnError(data));
            dataTable.setOwner(step);
            dataTableList.add(dataTable);
            step.getDataTower().setRoom(dataTable.getFloorIndex(), dataTable.getRoomIndex(), dataTable);
            dataTable.createPlugListeners();

            /*get data-file in data-table by id*/
            DataFile dataFile = step.getFile(dataTable.getDataFile().getId());
            dataTable.setDataFile(dataFile);
            dataFile.setOwner(dataTable);

            /*get column-list*/
            data = getData(ProjectFileType.DATA_COLUMN_LIST, project, 1, stepId, dataTableId);
            List<Integer> columnIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<DataColumn> columnList = new ArrayList<>();
            dataTable.setColumnList(columnList);

            /*get each column in column-list*/
            DataColumn dataColumn;
            for (Integer columnId : columnIdList) {
                data = getData(ProjectFileType.DATA_COLUMN, project, columnId, stepId, dataTableId);
                dataColumn = mapper.map((DataColumnData) throwExceptionOnError(data));
                dataColumn.setOwner(dataTable);
                columnList.add(dataColumn);
                dataColumn.createPlugListeners();
            }

            /*get output-list*/
            data = getData(ProjectFileType.DATA_OUTPUT_LIST, project, 1, stepId, dataTableId);
            List<Integer> outputIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<OutputFile> outputList = new ArrayList<>();
            dataTable.setOutputList(outputList);

            /*get each output in output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = getData(ProjectFileType.DATA_OUTPUT, project, outputId, stepId, dataTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(dataTable);
                outputList.add(outputFile);
            }

        }// end of for:DataTableIdList

        /*get transform-table-list*/
        data = getData(ProjectFileType.TRANSFORM_TABLE_LIST, project, 9, stepId);
        List<Integer> transformTableIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        List<TransformTable> transformTableList = new ArrayList<>();
        step.setTransformList(transformTableList);

        /*get each transform-table in transform-table-list*/
        for (Integer transformTableId : transformTableIdList) {
            data = getData(ProjectFileType.TRANSFORM_TABLE, project, transformTableId, stepId, 0, transformTableId);
            TransformTable transformTable = mapper.map((TransformTableData) throwExceptionOnError(data));
            transformTable.setOwner(step);
            transformTableList.add(transformTable);
            step.getTransformTower().setRoom(transformTable.getFloorIndex(), transformTable.getRoomIndex(), transformTable);
            transformTable.createPlugListeners();

            ColumnFxTable columnFxTable = transformTable.getColumnFxTable();
            step.getTransformTower().setRoom(columnFxTable.getFloorIndex(), columnFxTable.getRoomIndex(), columnFxTable);

            /*get transform-column-list*/
            data = getData(ProjectFileType.TRANSFORM_COLUMN_LIST, project, 1, stepId, 0, transformTableId);
            List<Integer> columnIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<DataColumn> columnList = new ArrayList<>();
            transformTable.setColumnList(columnList);

            /*get each transform-column in transform-column-list*/
            List<ColumnFx> columnFxList = columnFxTable.getColumnFxList();
            TransformColumn transformColumn;
            ColumnFx columnFx;
            for (Integer columnId : columnIdList) {
                data = getData(ProjectFileType.TRANSFORM_COLUMN, project, columnId, stepId, 0, transformTableId);
                transformColumn = mapper.map((TransformColumnData) throwExceptionOnError(data));
                transformColumn.setOwner(transformTable);
                columnList.add(transformColumn);
                transformColumn.createPlugListeners();

                /*get each transform-columnfx in transform-table(columnFxTable)*/
                ColumnFx fx = transformColumn.getFx();
                if (fx != null) {
                    data = getData(ProjectFileType.TRANSFORM_COLUMNFX, project, fx.getId(), stepId, 0, transformTableId);
                    columnFx = mapper.map((ColumnFxData) throwExceptionOnError(data));
                    columnFx.setOwner(transformColumn);
                    transformColumn.setFx(columnFx);
                    columnFxList.add(columnFx);
                    for (ColumnFxPlug columnFxPlug : columnFx.getEndPlugList()) {
                        columnFxPlug.setOwner(columnFx);
                    }
                    columnFx.createPlugListeners();
                }
            }

            /*get transform-output-list*/
            data = getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, project, 1, stepId, 0, transformTableId);
            List<Integer> outputIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<OutputFile> outputList = new ArrayList<>();
            transformTable.setOutputList(outputList);

            /*get each transform-output in transform-output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = getData(ProjectFileType.TRANSFORM_OUTPUT, project, outputId, stepId, 0, transformTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(transformTable);
                outputList.add(outputFile);
            }

            /*get tranformation-list*/
            data = getData(ProjectFileType.TRANSFORMATION_LIST, project, 1, stepId, 0, transformTableId);
            List<Integer> fxIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<TableFx> fxList = new ArrayList<>();
            transformTable.setFxList(fxList);

            /*get each tranformation in tranformation-list*/
            /*need Transformmation Model, find "addData(ProjectFileType.TRANSFORMATION" then use Mapper*/
            for (Integer fxId : fxIdList) {
                data = getData(ProjectFileType.TRANSFORMATION, project, 1, stepId, 0, transformTableId);
                TableFx tableFx = mapper.map((TableFxData) throwExceptionOnError(data));
                tableFx.setOwner(transformTable);
                fxList.add(tableFx);
            }

        }

        /*get line-list at the end*/
        data = getData(ProjectFileType.LINE_LIST, project, stepId, stepId);
        List<Integer> lineIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        List<Line> lineList = new ArrayList<>();
        step.setLineList(lineList);

        /*get each line in line-list*/
        for (Integer lineId : lineIdList) {
            data = getData(ProjectFileType.LINE, project, lineId, stepId);
            Line line = mapper.map((LineData) throwExceptionOnError(data));
            lineList.add(line);
        }

        /*regenerate selectableMap*/
        stepList.remove(stepIndex);
        stepList.add(stepIndex, step);
        project.setActiveStepIndex(step.getIndex());
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        log.warn("ProjectDataManager.getStep: selectableMap = {}", selectableMap);

        step.setActiveObject(selectableMap.get(stepData.getActiveObject()));
        for (Line line : lineList) {
            log.warn("line={}", line);
            line.setStartPlug(selectableMap.get(line.getStartSelectableId()).getStartPlug());

            try {
                line.setEndPlug(((HasEndPlug) selectableMap.get(line.getEndSelectableId())).getEndPlug());
            } catch (NullPointerException ex) {
                log.error("endSelectableId:{} not found", line.getEndSelectableId());
            }
        }

        return step;
    }

    private Object throwExceptionOnError(Object data) throws ProjectDataException {
        if (data instanceof Long) {
            throw new ProjectDataException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

    public Object getData(ProjectFileType fileType, Project project) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId());
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, Project project, String recordId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), recordId);
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, Project project, int recordId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, Project project, int recordId, int stepId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, Project project, int recordId, int stepId, int dataTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, Project project, int recordId, int stepId, int ignoredId, int transformTableId) {
        Workspace workspace = project.getOwner();
        KafkaRecordAttributes additional = new KafkaRecordAttributes(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, KafkaRecordAttributes additional) {
        log.warn("getData(fileType:{}, additional:{}", fileType, additional);

        long code = requestData(fileType, additional);
        if (code < 0) {
            return code;
        }

        Object data = captureData(fileType, additional);
        if (data == null) {
            log.error("getData.return null record");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        if (data instanceof Long) {
            long errorCode = (Long) data;
            KafkaErrorCode kafkaErrorCode = KafkaErrorCode.parse(errorCode);
            log.error("getData.return error({})", kafkaErrorCode);
            return errorCode;
        }

        KafkaRecord kafkaRecord = (KafkaRecord) data;
        Object object = kafkaRecord.getData();

        if (object == null) {
            log.error("getData.return null object");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }
        return object;
    }

    private long requestData(ProjectFileType fileType, KafkaRecordAttributes additional) {
        log.info("requestData( fileType:{}, recordId:{} )", fileType.name(), additional.getRecordId());

        if (!ready(producer)) {
            log.warn("requestData: producer not ready to send message");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        if (!readyToCapture(consumer, additional.getClientId())) {
            log.warn("requestData: consumer not ready to start capture");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        producer.send(new ProducerRecord<>(readTopic, fileType.name(), additional));
        return 1L;
    }

    private Object captureData(ProjectFileType fileType, KafkaRecordAttributes additional) {
        log.warn("captureData(fileType:{}, additional:{})", fileType, additional);

        /*TODO: timeout and maxTry need to load from configuration*/
        Object data = null;
        long timeout = 2000;
        long maxTry = 3;
        long retry = maxTry;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        boolean polling = true;
        boolean gotHeader = false;
        long clientId = additional.getClientId();
        while (polling) {

            records = consumer.poll(duration);
            if (records == null || records.count() == 0) {
                retry--;
                if (retry == 0) {
                    log.warn("exceed max try({}) stop consumer.poll.", maxTry);
                    polling = false;
                }
                continue;

            } else {
                retry = maxTry;
            }

            for (ConsumerRecord<String, byte[]> record : records) {
                byte[] value = record.value();
                String key = record.key();
                String offset = String.valueOf(record.offset());
                log.info("captureData: offset = {}, key = {}, value = {}", offset, key, Arrays.copyOf(value, 16));

                // find data message
                if (gotHeader) {
                    try {
                        data = deserializer.deserialize("", value);
                    } catch (Exception ex) {
                        log.error("Error when deserializing byte[] to object: ", ex);
                        polling = false;
                        break;
                    }
                    polling = false;
                    break;
                }

                // find header message

                if (!fileType.isMe(key)) {
                    // ignore other messages
                    log.warn("need header(clientId:{}, key:{}), ignore message( key:{} )", clientId, fileType, key);
                    continue;
                }

                if (value.length != 16) {
                    // ignore other messages
                    log.warn("need header(clientId:{}), ignore message by length={}", clientId, value.length);
                    continue;
                }

                long code = SerializeUtil.deserializeHeader(value);
                if (code < 0) {
                    // exit when error found
                    log.warn("need header(clientId:{}), stop capture by error-code({})", clientId, code);
                    data = code;
                    polling = false;
                    break;
                } else if (code != clientId) {
                    // ignore other messages
                    log.warn("need header(clientId:{}), ignore message by header(clientId:{})", clientId, code);
                    continue;
                }

                gotHeader = true;
            }
        }

        log.warn("captureData completed, data = {}", data == null ? "null" : "not null");
        return data;
    }
}
