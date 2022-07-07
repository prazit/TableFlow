package com.tflow.kafka;

import com.tflow.model.data.*;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.mapper.*;
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
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.*;

/*TODO: save updated object between line in RemoveDataFile when the Action RemoveDataFile is used in the UI*/
public class ProjectDataManager {

    private Logger log = LoggerFactory.getLogger(ProjectDataManager.class);

    private List<ProjectDataWriteBuffer> projectDataWriteBufferList = new ArrayList<>();

    private String writeTopic;
    private String readTopic;
    private String dataTopic;
    private long commitAgainMilliseconds;
    private boolean commitWaiting;
    private Producer<String, Object> producer;
    private Consumer<String, byte[]> consumer;

    /*TODO: remove Collection below, it for test*/
    public List<ProjectDataWriteBuffer> testBuffer = new ArrayList<>();

    public IdListMapper idListMapper;
    public ProjectMapper projectMapper;
    public DataSourceMapper dataSourceMapper;
    public StepMapper stepMapper;

    public ProjectDataManager() {
        createMappers();
    }

    private void createMappers() {
        idListMapper = new IdListMapper();
        projectMapper = Mappers.getMapper(ProjectMapper.class);
        dataSourceMapper = Mappers.getMapper(DataSourceMapper.class);
        stepMapper = Mappers.getMapper(StepMapper.class);
    }

    private void createProducer() {
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
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.encoding", "UTF-8");
        props.put("value.serializer", "com.tflow.kafka.ObjectSerializer");
        producer = new KafkaProducer<String, Object>(props);
    }

    private void createConsumer(long clientId) {
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

        subscribeTo(dataTopic, consumer = new KafkaConsumer<String, byte[]>(props));
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
        if (producer == null) createProducer();

        /*TODO: check status of Producer(kafka server)*/
        // need log.warn("something about server status");

        return true;
    }

    private boolean readyToCapture(Consumer<String, byte[]> consumer, long clientId) {
        if (consumer == null) createConsumer(clientId);

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
     */
    private void commit() {
        if (commitWaiting) return;

        ArrayList<ProjectDataWriteBuffer> commitList = new ArrayList<>(projectDataWriteBufferList);
        KafkaTWAdditional additional;
        ProjectFileType fileType;
        String recordId;
        KafkaRecordValue kafkaRecordValue;
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
            log.info("ProjectWriteCommand( fileType:{}, recordId:{} ) started.", fileType.name(), recordId);

            try {
                Object dataObject = writeCommand.getDataObject();
                byte[] serializedData = (dataObject == null) ? null : SerializeUtil.serialize(dataObject);
                kafkaRecordValue = new KafkaRecordValue(serializedData, additional);
                //value = SerializeUtil.serialize(kafkaRecordValue);
            } catch (IOException ex) {
                log.warn("Serialization failed: ", ex);
                commit(commitAgainMilliseconds);
                return;
            }

            producer.send(new ProducerRecord<String, Object>(writeTopic, key, kafkaRecordValue));
            projectDataWriteBufferList.remove(writeCommand);
            testBuffer.add(writeCommand);

            log.info("ProjectWriteCommand( fileType:{}, recordId:{} ) completed.", fileType.name(), recordId);
        }
    }

    public void addData(ProjectFileType fileType, Object object, Project project) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId());
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, Project project, String recordId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), recordId);
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, Project project, int recordId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId));
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId, int dataTableId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId, int ignoredId, int transformTableId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, object, additional);
    }

    public void addData(ProjectFileType fileType, Object object, KafkaTWAdditional additional) throws InvalidParameterException {
        if (additional.getModifiedUserId() <= 0) throw new InvalidParameterException("Required Field: ModifiedUserId");
        if (additional.getModifiedClientId() <= 0) throw new InvalidParameterException("Required Field: ModifiedClientId");

        // Notice: all of below copied from class com.flow.wcmd.UpdateProjectCommand.validate(String kafkaRecordKey, KafkaRecordValue kafkaRecordValue)
        int requireType = fileType.getRequireType();
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) throw new InvalidParameterException("Required Field: RecordId");
        if (additional.getProjectId() == null) throw new InvalidParameterException("Required Field: ProjectId");
        if (requireType > 1 && additional.getStepId() == null) throw new InvalidParameterException("Required Field: StepId");
        if (requireType == 3 && additional.getDataTableId() == null) throw new InvalidParameterException("Required Field: DataTableId");
        if (requireType == 4 && additional.getTransformTableId() == null) throw new InvalidParameterException("Required Field: TransformTableId");

        projectDataWriteBufferList.add(new ProjectDataWriteBuffer(fileType, object, additional));
        commit();
    }

    /* Notice: TFlow need to check Client file for heartbeat it self, TODO: need to remove Client file checker from TRcmd */
    @SuppressWarnings("unchecked")
    public Project getProject(String projectId, long userId, long clientId) throws ClassCastException, ProjectDataException {

        /*get project, to know the project is not edit by another */
        Object data = getData(ProjectFileType.PROJECT, new KafkaTWAdditional(clientId, userId, projectId, projectId));
        /*TODO: find "addData(ProjectFileType.PROJECT" then use Mapper*/
        Project project = projectMapper.map((ProjectData) throwExceptionOnError(data));

        /*get db-list*/
        data = getData(ProjectFileType.DB_LIST, new KafkaTWAdditional(clientId, userId, projectId, "1"));
        List<Integer> databaseIdList = (List<Integer>) throwExceptionOnError(data);
        Map<Integer, Database> databaseMap = new HashMap<>();
        project.setDatabaseMap(databaseMap);

        /*get each db in db-list*/
        for (Integer id : databaseIdList) {
            data = getData(ProjectFileType.DB, new KafkaTWAdditional(clientId, userId, projectId, String.valueOf(id)));
            databaseMap.put(id, dataSourceMapper.map((DatabaseData) throwExceptionOnError(data)));
        }

        /*get sftp-list*/
        data = getData(ProjectFileType.SFTP_LIST, new KafkaTWAdditional(clientId, userId, projectId, "2"));
        List<Integer> sftpIdList = (List<Integer>) throwExceptionOnError(data);
        Map<Integer, SFTP> sftpMap = new HashMap<>();
        project.setSftpMap(sftpMap);

        /*get each sftp in sftp-list*/
        for (Integer id : sftpIdList) {
            data = getData(ProjectFileType.SFTP, new KafkaTWAdditional(clientId, userId, projectId, String.valueOf(id)));
            sftpMap.put(id, dataSourceMapper.map((SFTPData) throwExceptionOnError(data)));
        }

        /*get local-list*/
        data = getData(ProjectFileType.LOCAL_LIST, new KafkaTWAdditional(clientId, userId, projectId, "3"));
        List<Integer> localIdList = (List<Integer>) throwExceptionOnError(data);
        Map<Integer, Local> localMap = new HashMap<>();
        project.setLocalMap(localMap);

        /*get each local in local-list*/
        for (Integer id : localIdList) {
            data = getData(ProjectFileType.LOCAL, new KafkaTWAdditional(clientId, userId, projectId, String.valueOf(id)));
            localMap.put(id, dataSourceMapper.map((LocalData) throwExceptionOnError(data)));
        }

        /*get step-list*/
        data = getData(ProjectFileType.STEP_LIST, new KafkaTWAdditional(clientId, userId, projectId, "4"));
        List<StepItemData> stepItemDataList = (List<StepItemData>) throwExceptionOnError(data);
        project.setStepList(projectMapper.toStepList(stepItemDataList));

        return project;
    }

    private Step getStep(long clientId, long userId, Project project, int stepIndex) throws Exception {
        /*TODO: all step below need to create Model and Mapper(mapstruct lib)*/

        String projectId = project.getId();
        List<Step> stepList = project.getStepList();
        Step stepModel = stepList.get(stepIndex);
        String stepId = String.valueOf(stepModel.getId());

        /*get step*/
        Object data = getData(ProjectFileType.STEP, new KafkaTWAdditional(clientId, userId, projectId, stepId, stepId));
        /*TODO: need STEP Model and Mapper, find "addData(ProjectFileType.STEP" then use Mapper*/
        stepList.remove(stepIndex);
        Step step = (Step) throwExceptionOnError(data);
        stepList.add(stepIndex, step);

        /*TODO: get each tower in step*/
        /*TODO: get each floor in tower*/

        /*TODO: get data-table-list*/
        /*TODO: get each data-table in data-table-list*/

        /*TODO: get data-file in data-table*/

        /*TODO: get column-list*/
        /*TODO: get each column in column-list*/

        /*TODO: get output-list*/
        /*TODO: get each output in output-list*/

        /*TODO: TRANSFORM TABLE need list*/
        /*TODO: get transform-table-list*/
        /*TODO: get each transform-table in transform-table-list*/

        /*TODO: get tranform-column-list*/
        /*TODO: get each tranform-column in tranform-column-list*/

        /*TODO: get each tranform-columnfx in tranform-table(columnFxTable)*/

        /*TODO: get tranform-output-list*/
        /*TODO: get each tranform-output in tranform-output-list*/

        /*TODO: get tranformation-list*/
        /*TODO: get each tranformation in tranformation-list*/

        /*TODO: get line-list at the end*/
        /*TODO: get each line in line-list*/

        return step;
    }

    private Object throwExceptionOnError(Object data) throws ProjectDataException {
        if (data instanceof Long) {
            throw new ProjectDataException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

    public Object getData(ProjectFileType fileType, KafkaTWAdditional additional) {
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

        Object object = null;
        try {
            KafkaRecordValue kafkaRecordValue = (KafkaRecordValue) data;
            Object serialized = kafkaRecordValue.getData();
            if (serialized instanceof String) {
                object = SerializeUtil.deserialize((String) kafkaRecordValue.getData());
            } else {
                object = SerializeUtil.deserialize((byte[]) kafkaRecordValue.getData());
            }
        } catch (Exception ex) {
            log.error("getData.deserialize error: {}", ex.getMessage());
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        if (object == null) {
            log.error("getData.return null object");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }
        return object;
    }

    private long requestData(ProjectFileType fileType, KafkaTWAdditional additional) {
        log.info("requestData( fileType:{}, recordId:{} ) started.", fileType.name(), additional.getRecordId());

        if (!ready(producer)) {
            log.warn("requestData: producer not ready to send message");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        if (!readyToCapture(consumer, additional.getModifiedClientId())) {
            log.warn("requestData: consumer not ready to start capture");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        producer.send(new ProducerRecord<String, Object>(readTopic, fileType.name(), additional));
        log.info("requestData completed.");

        return 1L;
    }

    private Object captureData(ProjectFileType fileType, KafkaTWAdditional additional) {
        log.warn("captureData(fileType:{}, additional:{}) started", fileType, additional);

        Object data = null;
        long timeout = 30000;
        long maxTry = 3;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        boolean polling = true;
        boolean gotHeader = false;
        long clientId = additional.getModifiedClientId();
        while (polling) {

            records = consumer.poll(duration);
            if (records == null) {
                log.warn("consumer.poll return null!");
                maxTry--;
                if (maxTry == 0) {
                    log.warn("exceed max try({}) stop consumer.poll.", maxTry);
                    polling = false;
                }
                continue;
            } else {
                log.warn("consumer.poll return {} record(s).", records.count());
            }

            for (ConsumerRecord<String, byte[]> record : records) {
                byte[] value = record.value();
                String key = record.key();
                String offset = String.valueOf(record.offset());
                log.info("captureData: offset = {}, key = {}, value = {}", offset, key, Arrays.copyOf(value, 16));

                // find data message
                if (gotHeader) {
                    log.warn("try to deserialize data message.");
                    try {
                        data = SerializeUtil.deserialize(value);
                    } catch (Exception ex) {
                        log.error("Error when deserializing byte[] to object: ", ex);
                        polling = false;
                        break;
                    }

                    log.warn("got data message.");
                    polling = false;
                    break;
                }

                // find header message
                log.warn("try to deserialize header message.");

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
                log.warn("got header message.");
            }
        }

        log.warn("captureData completed, data = {}", data == null ? "null" : "not null");
        return data;
    }

}
