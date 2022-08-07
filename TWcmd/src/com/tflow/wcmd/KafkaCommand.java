package com.tflow.wcmd;

import com.tflow.file.SerializeReader;
import com.tflow.file.SerializeWriter;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.util.FileUtil;
import org.apache.kafka.common.errors.SerializationException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Receive kafka message then deserialize and execute something.
 */
public abstract class KafkaCommand {

    protected String key;
    protected Object value;
    protected EnvironmentConfigs environmentConfigs;

    public KafkaCommand(String key, Object value, EnvironmentConfigs environmentConfigs) {
        this.key = key;
        this.value = value;
        this.environmentConfigs = environmentConfigs;
    }

    public abstract void info(String message, Object... objects);

    public abstract void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException;
}
