package com.tflow.tbcmd;

import com.tflow.kafka.KafkaTopics;
import com.tflow.model.data.DataManager;
import com.tflow.system.CLIbase;
import com.tflow.util.SerializeUtil;
import com.tflow.zookeeper.AppName;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Collections;

public class TBcmd extends CLIbase {

    public TBcmd() {
        super(AppName.PACKAGE_BUILDER);
    }

    @Override
    protected void loadConfigs() throws Exception {
        /*add more Fixed configuration for Consumer*/
        configs.put("consumer.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("consumer.key.deserializer.encoding", StandardCharsets.UTF_8.name());
        configs.put("consumer.value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    }

    @SuppressWarnings("unchecked")
    public void run() {

        DataManager dataManager = new DataManager(environment, "TBcmd", zkConfiguration);
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(getProperties("consumer.", configs));

        /*TODO: need to load topicBuild from configuration*/
        String topicBuild = KafkaTopics.PROJECT_BUILD.getTopic();
        consumer.subscribe(Collections.singletonList(topicBuild));
        log.info("Subscribed to topicBuild " + topicBuild);

        Deserializer deserializer = null;
        try {
            deserializer = SerializeUtil.getDeserializer(environmentConfigs.getKafkaDeserializer());
        } catch (Exception ex) {
            log.error("Deserializer creation error: " + ex.getMessage());
            log.trace("", ex);
            return;
        }

        long timeout = 30000;
        Duration duration = Duration.ofMillis(timeout);
        ConsumerRecords<String, byte[]> records;
        polling = true;
        while (polling) {
            records = consumer.poll(duration);

            if (records.count() == 0) continue;

            terminateOnNewerVersion();

            for (ConsumerRecord<String, byte[]> record : records) {
                long offset = record.offset();
                String key = record.key();
                Object value;

                try {
                    value = deserializer.deserialize("", record.value());
                } catch (Exception ex) {
                    log.warn("Skip invalid message={}", new String(record.value(), StandardCharsets.ISO_8859_1));
                    log.warn("Deserialize error: ", ex);
                    continue;
                }

                /*TODO: future feature: add command to UpdateProjectCommandQueue*/
                BuildPackageCommand buildPackageCommand = new BuildPackageCommand(offset, key, value, environmentConfigs, dataManager);
                log.info("Incoming message: {}", buildPackageCommand.toString());

                /*TODO: future feature: move this execute block into UpdateProjectCommandQueue*/
                try {
                    buildPackageCommand.execute();
                    log.info("Incoming message completed: {}", buildPackageCommand.toString());
                } catch (InvalidParameterException inex) {
                    /*Notice: how to handle rejected command, can rebuild when percentComplete < 100*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
                } catch (UnsupportedOperationException ex) {
                    /*Notice: how to handle rejected command, can rebuild when percentComplete < 100*/
                    log.error("UnsupportedOperationException: {}", ex.getMessage());
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
                } catch (Exception ex) {
                    log.error("Unexpected error occur: " + ex.getMessage());
                    log.trace("", ex);
                    log.warn("Message rejected: {}", buildPackageCommand.toString());
                } finally {
                    dataManager.waitAllTasks();
                }
            }
        }

        consumer.close();
    }

}
