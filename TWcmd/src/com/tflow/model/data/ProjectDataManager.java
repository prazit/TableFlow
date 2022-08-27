package com.tflow.model.data;

import com.tflow.kafka.*;
import com.tflow.system.Environment;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import com.tflow.zookeeper.ZKConfiguration;
import com.tflow.zookeeper.ZKConfigNode;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.zookeeper.KeeperException;
import org.mapstruct.ap.shaded.freemarker.template.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/*TODO: save updated object between line in RemoveDataFile when the Action RemoveDataFile is used in the UI*/
public class ProjectDataManager {

    private Logger log = LoggerFactory.getLogger(ProjectDataManager.class);

    private Map<String, ProjectDataWriteBuffer> projectDataWriteBufferList;

    EnvironmentConfigs environmentConfigs;
    private String writeTopic;
    private String readTopic;
    private String dataTopic;
    private long maximumTransactionId;
    private long commitAgainMilliseconds;
    private int kafkaTimeout;
    private boolean commitWaiting;
    private Producer<String, Object> producer;

    private String consumerGroupId;
    private Consumer<String, byte[]> consumer;
    private ArrayList<TopicPartition> topicPartitionArrayList;

    private Deserializer deserializer;

    private Thread commitThread;

    private ZKConfiguration globalConfigs;

    public ProjectDataManager(Environment environment, String consumerGroupId, ZKConfiguration zkConfiguration) {
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
        this.consumerGroupId = consumerGroupId;
        this.globalConfigs = zkConfiguration;
        projectDataWriteBufferList = new HashMap<>();
        loadConfigs();
    }

    private void loadConfigs() {
        /*TODO: need config for commitAgainMilliseconds*/
        commitAgainMilliseconds = 10000;
        kafkaTimeout = 10000;

        try {
            log.trace("connecting to zookeeper");
            if (!globalConfigs.isConnected()) globalConfigs.connect();
            log.trace("connected to zookeeper");
        } catch (Exception ex) {
            log.error("connect to zookeeper failed!", ex);
            return;
        }

        try {
            maximumTransactionId = globalConfigs.getLong(ZKConfigNode.MAXIMUM_TRANSACTION_ID);
        } catch (InterruptedException ex) {
            log.error("loadConfigs failed! ", ex);
        }
    }

    private long newTransactionId() throws InterruptedException {
        return newTransactionId(1);
    }

    /**
     * @return first transactionId.
     */
    private long newTransactionId(int size) throws InterruptedException {
        long lastTransId = globalConfigs.getLong(ZKConfigNode.LAST_TRANSACTION_ID);

        long setLastTransId = lastTransId + size;
        if (setLastTransId > maximumTransactionId) {
            lastTransId = 1;
            setLastTransId = lastTransId + size;
        }
        log.debug("get lastTransId from Zookeeper = {}", lastTransId);

        globalConfigs.set(ZKConfigNode.LAST_TRANSACTION_ID, setLastTransId);
        log.debug("set value to lastTransId = {} success", setLastTransId);

        return lastTransId + 1;
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
        writeTopic = KafkaTopics.PROJECT_WRITE.getTopic();
        readTopic = KafkaTopics.PROJECT_READ.getTopic();
        dataTopic = KafkaTopics.PROJECT_DATA.getTopic();
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("request.timeout.ms", kafkaTimeout);
        props.put("transaction.timeout.ms", kafkaTimeout);
        props.put("default.api.timeout.ms", kafkaTimeout);
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
        props.put("request.timeout.ms", kafkaTimeout);
        props.put("transaction.timeout.ms", kafkaTimeout);
        props.put("default.api.timeout.ms", kafkaTimeout);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        props.put("group.id", consumerGroupId + clientId);

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
        log.trace("subscribeTo(topic:{}).", topic);
        List<PartitionInfo> topicPartitionList = consumer.partitionsFor(topic);
        topicPartitionArrayList = new ArrayList<>();

        for (PartitionInfo partitionInfo : topicPartitionList) {
            topicPartitionArrayList.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }
        log.trace("ready to assign/subscribe.");

        //need to use assign instead of subscribe: consumer.subscribe(Collections.singletonList(topic));
        consumer.assign(topicPartitionArrayList);
        log.info("topic '{}' is assigned/subscribed", dataTopic);

        consumer.poll(Duration.ofMillis(0));
        consumer.seekToEnd(topicPartitionArrayList);

        for (TopicPartition topicPartition : topicPartitionArrayList) {
            log.info("partition:{}, position:{}", topicPartition, consumer.position(topicPartition) - 1);
        }
        log.trace("offset moved to last message.");
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

    private boolean readyToCapture(Consumer<String, byte[]> consumer, long clientId, KafkaRecordAttributes additional) {
        if (consumer == null && !createConsumer(clientId)) {
            return false;
        }

        /* need transId for capture */
        long transactionId;
        try {
            transactionId = newTransactionId();
        } catch (InterruptedException e) {
            return false;
        }
        additional.setTransactionId(transactionId);

        /* Notice: exceed maximum poll when seekToEnd before poll.
        if (consumer != null) consumer.seekToEnd(topicPartitionArrayList);*/

        /*TODO: check status of Consumer(kafka server)*/
        // need log.warn("something about server status");

        return true;
    }

    /**
     * Wait milliseconds and then commit.
     */
    private void commit(long milliseconds) {
        commitWaiting = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*Wait milliseconds, please use Scheduler or new Thread and Wait.*/
                    log.trace("commit again in {} milliseconds by thread {}", milliseconds, Thread.currentThread().getName());
                    Thread.sleep(milliseconds);
                } catch (InterruptedException ex) {
                    log.error("InterruptedException occurred during commit(ms:" + milliseconds + ")", ex);
                }

                /*after wait, need to set commitWaiting = false; and then commit*/
                commitWaiting = false;
                log.trace("call commit by thread {}", Thread.currentThread().getName());
                commit();
            }
        }, "Waiting-Thread");
        thread.start();
    }

    /**
     * TODO: need to update Client-Data-file before execute first command
     */
    private void commit() {
        if (commitWaiting || commitThread != null) return;

        commitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.trace("commitInThread: {}", Thread.currentThread().getName());
                if (!commitInThread()) {
                    commit(commitAgainMilliseconds);
                    return;
                }
                resetCommitThread();
            }
        }, "Commit-Thread");
        commitThread.start();
    }

    private void resetCommitThread() {
        commitThread.interrupt();
        commitThread = null;
    }

    public boolean commitInThread() {
        ArrayList<ProjectDataWriteBuffer> commitList = new ArrayList<>(projectDataWriteBufferList.values());
        KafkaRecordAttributes additional;
        ProjectFileType fileType;
        KafkaRecord kafkaRecord;
        String key;
        commitList.sort((t1, t2) -> Integer.compare(t1.getIndex(), t2.getIndex()));

        if (!ready(producer)) {
            log.error("commitInTread: producer not ready, try to commit again next {} ms", commitAgainMilliseconds);
            commit(commitAgainMilliseconds);
            return false;
        }

        long transactionId;
        try {
            transactionId = newTransactionId(commitList.size());
        } catch (InterruptedException ex) {
            log.error("newTransactionId failed!", ex);
            return false;
        }

        for (ProjectDataWriteBuffer writeCommand : commitList) {
            additional = writeCommand.getAdditional();
            additional.setTransactionId(transactionId);
            transactionId++;

            fileType = writeCommand.getFileType();
            key = fileType.name();
            kafkaRecord = new KafkaRecord(writeCommand.getDataObject(), additional);
            log.info("Outgoing message: write({})", writeCommand);

            Future<RecordMetadata> future = producer.send(new ProducerRecord<>(writeTopic, key, kafkaRecord));
            if (!isSuccess(future)) {
                return false;
            }

            projectDataWriteBufferList.remove(writeCommand.uniqueKey());
        }

        return true;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, List idList, ProjectUser project) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId());
        addData(fileType, idList, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId());
        addData(fileType, (Object) object, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, List idList, ProjectUser project, String recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), recordId);
        addData(fileType, idList, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project, String recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), recordId);
        addData(fileType, (Object) object, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId));
        addData(fileType, (Object) object, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, idList, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, (Object) object, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId, int dataTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, idList, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId, int dataTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, (Object) object, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, List idList, ProjectUser project, int recordId, int stepId, int ignoredId, int transformTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, idList, additional);
        return additional;
    }

    public KafkaRecordAttributes addData(ProjectFileType fileType, TWData object, ProjectUser project, int recordId, int stepId, int ignoredId, int transformTableId) {
        KafkaRecordAttributes additional = new KafkaRecordAttributes(project.getClientId(), project.getUserId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, (Object) object, additional);
        return additional;
    }

    private void addData(ProjectFileType fileType, TWData object, KafkaRecordAttributes additional) throws InvalidParameterException {
        addData(fileType, (Object) object, additional);
    }

    private void addData(ProjectFileType fileType, Object object, KafkaRecordAttributes additional) throws InvalidParameterException {
        validate(fileType, additional);

        ProjectDataWriteBuffer projectDataWriteBuffer = new ProjectDataWriteBuffer(projectDataWriteBufferList.size(), fileType, object, additional);
        projectDataWriteBufferList.put(projectDataWriteBuffer.uniqueKey(), projectDataWriteBuffer);

        commit();
    }

    private void validate(ProjectFileType fileType, KafkaRecordAttributes additional) {
        if (additional.getUserId() < 0) throw new InvalidParameterException("Invalid Value(" + additional.getUserId() + "): ModifiedUserId for " + fileType);
        if (additional.getClientId() < 0) throw new InvalidParameterException("Invalid Value(" + additional.getClientId() + "): ModifiedClientId for " + fileType);

        // Notice: all of below copied from class com.flow.wcmd.UpdateProjectCommand.validate(String kafkaRecordKey, KafkaRecordValue kafkaRecordValue)
        int requireType = fileType.getRequireType();
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) throw new InvalidParameterException("Required Field: RecordId for " + fileType);
        if (requireType > 0 && additional.getProjectId() == null) throw new InvalidParameterException("Required Field: ProjectId for " + fileType);
        if (requireType > 1 && requireType < 9 && additional.getStepId() == null) throw new InvalidParameterException("Required Field: StepId for " + fileType);
        if (requireType == 3 && additional.getDataTableId() == null) throw new InvalidParameterException("Required Field: DataTableId for " + fileType);
        if (requireType == 4 && additional.getTransformTableId() == null) throw new InvalidParameterException("Required Field: TransformTableId for " + fileType);

        additional.setModifiedDate(DateTimeUtil.now());
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
        validate(fileType, additional);

        long code = requestData(fileType, additional);
        if (code < 0) {
            return code;
        }

        /*headerData used instead of TransactionID (uniqueKeys: time, userId, clientId, projectId)*/
        Object data = captureData(fileType, getHeaderData(additional));
        if (data == null) {
            log.error("getData.return null record, no response from read-service!");
            return KafkaErrorCode.READ_SERVICE_NO_RESPONSE.getCode();
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

    private HeaderData getHeaderData(KafkaRecordAttributes additional) {
        HeaderData headerData = new HeaderData();
        headerData.setProjectId(additional.getProjectId());
        headerData.setUserId(additional.getUserId());
        headerData.setClientId(additional.getClientId());
        headerData.setTime(additional.getModifiedDate().getTime());
        headerData.setTransactionId(additional.getTransactionId());
        return headerData;
    }

    private long requestData(ProjectFileType fileType, KafkaRecordAttributes additional) {

        if (!ready(producer)) {
            log.warn("requestData: producer not ready to send message");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        if (!readyToCapture(consumer, additional.getClientId(), additional)) {
            log.warn("requestData: consumer not ready to start capture");
            return KafkaErrorCode.INTERNAL_SERVER_ERROR.getCode();
        }

        log.trace("Outgoing message: read(fileType:{}, additional:{}", fileType, additional);
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(readTopic, fileType.name(), additional));
        if (isSuccess(future)) {
            return 1L;
        }
        return -1L;
    }

    private Object captureData(ProjectFileType fileType, HeaderData headerData) {
        log.trace("captureData(fileType:{}, header:{})", fileType, headerData);

        /*TODO: timeout and maxTry need to load from configuration*/
        Object capturedData = null;
        long timeout = 2000;
        long maxTry = 9;
        long retry = maxTry;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        boolean polling = true;
        boolean gotHeader = false;
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
                Object captured;
                try {
                    captured = deserializer.deserialize("", value);
                } catch (Exception ex) {
                    log.error("Error when deserializing byte[] to object: ", ex);
                    polling = false;
                    break;
                }
                log.info("captureData: offset = {}, key = {}, captured-data = {}:{}", offset, key, captured.getClass().getSimpleName(), captured);

                // find data message
                if (gotHeader) {
                    try {
                        capturedData = (KafkaRecord) captured;
                    } catch (ClassCastException ex) {
                        log.error("Error when cast captured-data to KafkaRecord: ", ex);
                        polling = false;
                        break;
                    }
                    polling = false;
                    break;
                }

                // find header message
                HeaderData capturedHeader;
                try {
                    capturedHeader = (HeaderData) captured;
                } catch (ClassCastException ex) {
                    log.error("Error when cast captured-data to HeaderData: ", ex);
                    continue;
                }

                if (!isMyHeader(headerData, capturedHeader)) {
                    // ignore other messages
                    log.warn("ignore another header({})", capturedHeader);
                    continue;
                }

                if (capturedHeader.getResponseCode() < 0) {
                    capturedData = capturedHeader.getResponseCode();
                    polling = false;
                    break;
                }

                gotHeader = true;
            }
        } //end of While

        log.debug("captureData completed, data = {}", capturedData);
        return capturedData;
    }

    private boolean isMyHeader(HeaderData headerData, HeaderData capturedHeader) {
        String myProjectId = headerData.getProjectId();
        String projectId = capturedHeader.getProjectId();
        return headerData.getTime() == capturedHeader.getTime() &&
                ((myProjectId == null && projectId == null) || (myProjectId != null && myProjectId.compareTo(projectId) == 0)) &&
                headerData.getClientId() == capturedHeader.getClientId() &&
                headerData.getUserId() == capturedHeader.getUserId() &&
                headerData.getTransactionId() == capturedHeader.getTransactionId();
    }

    private boolean isSuccess(Future<RecordMetadata> future) {
        while (!future.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                /*nothing*/
            }
        }

        boolean result;
        if (future.isCancelled()) {
            log.error("Record Cancelled by Kafka");
            result = false;
        } else try {
            RecordMetadata recordMetadata = future.get();
            result = true;
        } catch (InterruptedException ex) {
            log.error("InterruptedException: ", ex);
            result = false;
        } catch (ExecutionException ex) {
            log.error("ExecutionException: ", ex);
            result = false;
        }

        return result;
    }

    public boolean isClosable() {
        return projectDataWriteBufferList.size() == 0 && commitThread == null && !commitWaiting;
    }

}
