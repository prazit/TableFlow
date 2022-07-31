package com.tflow.kafka;

import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.system.Environment;
import com.tflow.util.DateTimeUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public ProjectDataManager(Environment environment) {
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
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

    public void addData(ProjectFileType fileType, List<Integer> idList, ProjectUser project) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId());
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId());
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, ProjectUser project, String recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), recordId);
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project, String recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), recordId);
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId, int dataTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId, int dataTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, (Object) object, additional);
    }

    public void addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId, int ignoredId, int transformTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, idList, additional);
    }

    public void addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId, int ignoredId, int transformTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
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

        additional.setModifiedDate(DateTimeUtil.now());
        projectDataWriteBufferList.add(new ProjectDataWriteBuffer(fileType, object, additional));

        commit();
    }

    public Object getData(ProjectFileType fileType, ProjectUser project) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId());
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, ProjectUser project, String recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), recordId);
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, ProjectUser project, int recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, ProjectUser project, int recordId, int stepId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, ProjectUser project, int recordId, int stepId, int dataTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        return getData(fileType, additional);
    }

    public Object getData(ProjectFileType fileType, ProjectUser project, int recordId, int stepId, int ignoredId, int transformTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
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
