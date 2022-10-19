package com.tflow.wcmd;

import com.tflow.kafka.KafkaTopics;
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

public class TWcmd extends CLIbase {

    public TWcmd() {
        super(AppName.DATA_WRITER);
    }

    @Override
    protected void loadConfigs() throws Exception {
        /*add more Fixed configuration for Consumer*/
        configs.put("consumer.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        configs.put("consumer.key.deserializer.encoding", StandardCharsets.UTF_8.name());
        configs.put("consumer.value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    }

    @Override
    public void run() {

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(getProperties("consumer.", configs));

        String topic = KafkaTopics.PROJECT_WRITE.getTopic();
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic " + topic);

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
                    value = deserializer.deserialize(topic, record.value());
                } catch (Exception ex) {
                    log.warn("Skip invalid message={}", new String(record.value(), StandardCharsets.ISO_8859_1));
                    log.warn("Deserialize error: ", ex);
                    continue;
                }

                /*TODO: add command to UpdateProjectCommandQueue*/
                UpdateProjectCommand updateProjectCommand = new UpdateProjectCommand(offset, key, value, environmentConfigs);
                log.info("Incoming message: {}", updateProjectCommand);

                /*TODO: move this execute block into UpdateProjectCommandQueue*/
                try {
                    updateProjectCommand.execute();
                    log.info("Incoming message completed: {}", updateProjectCommand);

                    /*TODO: IMPORTANT: after success need to commit consumer-group-offset to know its already done to avoid duplicated commands*/

                } catch (InvalidParameterException inex) {
                    /*TODO: how to handle rejected command*/
                    log.error("Invalid parameter: {}", inex.getMessage());
                    log.warn("Message rejected: {}", updateProjectCommand.toString());
                } catch (Exception ex) {
                    log.error("Hard error: " + ex.getMessage());
                    log.trace("", ex);
                    log.warn("Message rejected: {}", updateProjectCommand.toString());
                }

            }
        }

        consumer.close();
    }

}
