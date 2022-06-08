package com.tflow.kafka;

import com.tflow.model.editor.action.Action;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.List;

@KafkaClient
public interface KafkaMessenger {

    @Topic("quickstart-events")
    void sendMessage(@KafkaKey String key, List<Action> value);

}
