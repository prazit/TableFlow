package com.tflow.wcmd;

import com.tflow.kafka.EnvironmentConfigs;

import java.io.IOException;

/**
 * Receive kafka message then deserialize and execute something.
 */
public abstract class KafkaCommand {

    protected long offset;
    protected String key;
    protected Object value;
    protected EnvironmentConfigs environmentConfigs;

    public KafkaCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs) {
        this.offset = offset;
        this.key = key;
        this.value = value;
        this.environmentConfigs = environmentConfigs;
    }

    public abstract void info(String message, Object... objects);

    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "offset:" + offset +
                ", key:'" + key + '\'' +
                ", value:" + value +
                '}';
    }
}
