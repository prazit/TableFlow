package com.tflow.tbcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaTopics;
import com.tflow.model.data.ProjectDataManager;
import com.tflow.system.Environment;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TBcmd {

    private Logger log = LoggerFactory.getLogger(TBcmd.class);

    private boolean polling;

    private Environment environment;
    private EnvironmentConfigs environmentConfigs;

    public TBcmd() {
        environment = Environment.DEVELOPMENT;
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
    }

    @SuppressWarnings("unchecked")
    public void start() {

        ProjectDataManager projectDataManager = new ProjectDataManager(environment);
        KafkaConsumer<String, byte[]> consumer = createConsumer();

        /*TODO: need to load topicBuild from configuration*/
        String topicBuild = KafkaTopics.PROJECT_BUILD.getTopic();
        consumer.subscribe(Collections.singletonList(topicBuild));
        log.info("Subscribed to topicBuild " + topicBuild);

        Deserializer deserializer = null;
        try {
            deserializer = SerializeUtil.getDeserializer(environmentConfigs.getKafkaDeserializer());
        } catch (Exception ex) {
            log.error("Deserializer creation error: ", ex);
            return;
        }

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            for (ConsumerRecord<String, byte[]> record : records) {

                Object value;
                String key = record.key();
                String offset = String.valueOf(record.offset());
                log.info("Incoming message offset: {}, key: {}.", offset, key);

                try {
                    value = deserializer.deserialize("", record.value());
                } catch (Exception ex) {
                    log.warn("Skip invalid message={}", new String(record.value(), StandardCharsets.ISO_8859_1));
                    log.warn("Deserialize error: ", ex);
                    continue;
                }

                /*TODO: future feature: add command to UpdateProjectCommandQueue*/
                BuildPackageCommand buildPackageCommand = new BuildPackageCommand(key, value, environmentConfigs, projectDataManager);

                /*TODO: future feature: move this execute block into UpdateProjectCommandQueue*/
                try {
                    buildPackageCommand.execute();
                    log.info("buildPackageCommand completed.");
                } catch (InvalidParameterException inex) {
                    /*Notice: how to handle rejected command, can rebuild when percentComplete < 100*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.info("buildPackageCommand(offset: {}, key: {}) rejected.", offset, key);
                } catch (Exception ex) {
                    log.error("Hard error: ", ex);
                    log.info("buildPackageCommand(offset: {}, key: {}) rejected.", offset, key);
                }
            }
        }

        consumer.close();
    }

    private KafkaConsumer<String, byte[]> createConsumer() {
        /*TODO: need to load consumer configuration*/
        environment = Environment.DEVELOPMENT;
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
        Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("group.id", "tbcmd");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer.encoding", "UTF-8");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer<>(props);
    }

    public void stop() {
        polling = false;
    }

}
