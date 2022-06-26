package com.tflow.kafka;

import com.tflow.model.editor.Project;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.server.ServerNotActiveException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*TODO: need FileType for columnFx and then use it instead of TRANSFORM_COLUMN*/
/*TODO: need some missed commands for Undo of Add commands below:
 + RemoveTransformColumn for AddTransformColumn
 + RemoveOutputFile for AddOutputFile
 + RemoveTableFx for AddTableFx
 */

/*TODO: how to update Object between line*/
public class ProjectDataManager {

    private static Logger log = LoggerFactory.getLogger(ProjectDataManager.class);

    private static List<ProjectWriteCommand> projectWriteCommandList = new ArrayList<>();

    private static String writeTopic;
    private static long commitAgainMilliseconds;
    private static boolean commitWaiting;
    private static Producer<String, String> producer;

    private static void createProducer() {
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
        writeTopic = "quickstart-events";
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.encoding", "UTF-8");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer.encoding", "UTF-8");
        producer = new KafkaProducer<String, String>(props);
    }

    private static boolean ready(Producer<String, String> producer) {
        if (producer == null) createProducer();

        /*TODO: check status of Producer(kafka server)*/
        // need log.warn("something about server status");

        return true;
    }

    /**
     * Wait milliseconds and then commit.
     */
    private static void commit(long milliseconds) {
        commitWaiting = true;

        /*TODO: Wait milliseconds, please use Scheduler.*/
        /*TODO: after wait, need to set commitWaiting = false; and then commit*/
    }

    private static void commit() {
        if (commitWaiting) return;

        ArrayList<ProjectWriteCommand> commitList = new ArrayList<>(projectWriteCommandList);
        KafkaTWAdditional additional;
        ProjectFileType fileType;
        String recordId;
        KafkaRecordValue kafkaRecordValue;
        String key;
        String value;
        for (ProjectWriteCommand writeCommand : commitList) {
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
                String serializedData = (dataObject == null) ? null : SerializeUtil.serialize(dataObject);
                kafkaRecordValue = new KafkaRecordValue(serializedData, additional);
                value = SerializeUtil.serialize(kafkaRecordValue);
            } catch (IOException ex) {
                log.warn("Serialization failed: ", ex);
                commit(commitAgainMilliseconds);
                return;
            }

            producer.send(new ProducerRecord<String, String>(writeTopic, key, value));
            projectWriteCommandList.remove(writeCommand);

            log.info("ProjectWriteCommand( fileType:{}, recordId:{} ) completed.", fileType.name(), recordId);
        }
    }

    public static void addData(ProjectFileType fileType, Object object, Project project) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId());
        addData(fileType, object, additional);
    }

    public static void addData(ProjectFileType fileType, Object object, Project project, int recordId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId));
        addData(fileType, object, additional);
    }

    public static void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        addData(fileType, object, additional);
    }

    public static void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId, int dataTableId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setDataTableId(String.valueOf(dataTableId));
        addData(fileType, object, additional);
    }

    public static void addData(ProjectFileType fileType, Object object, Project project, int recordId, int stepId, int ignoredId, int transformTableId) {
        Workspace workspace = project.getOwner();
        KafkaTWAdditional additional = new KafkaTWAdditional(workspace.getClient().getId(), workspace.getUser().getId(), project.getId(), String.valueOf(recordId), String.valueOf(stepId));
        additional.setTransformTableId(String.valueOf(transformTableId));
        addData(fileType, object, additional);
    }

    public static void addData(ProjectFileType fileType, Object object, KafkaTWAdditional additional) throws InvalidParameterException {
        if (additional.getModifiedUserId() <= 0) throw new InvalidParameterException("Required Field: ModifiedUserId");
        if (additional.getModifiedClientId() <= 0) throw new InvalidParameterException("Required Field: ModifiedClientId");

        // Notice: all of below copied from class com.flow.wcmd.UpdateProjectCommand.validate(String kafkaRecordKey, KafkaRecordValue kafkaRecordValue)
        int requireType = fileType.getRequireType();
        if (!fileType.getPrefix().endsWith("list") && additional.getRecordId() == null) throw new InvalidParameterException("Required Field: RecordId");
        if (additional.getProjectId() == null) throw new InvalidParameterException("Required Field: ProjectId");
        if (requireType > 1 && additional.getStepId() == null) throw new InvalidParameterException("Required Field: StepId");
        if (requireType == 3 && additional.getDataTableId() == null) throw new InvalidParameterException("Required Field: DataTableId");
        if (requireType == 4 && additional.getTransformTableId() == null) throw new InvalidParameterException("Required Field: TransformTableId");

        projectWriteCommandList.add(new ProjectWriteCommand(fileType, object, additional));
        commit();
    }
}
