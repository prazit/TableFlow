package com.tflow.file;

import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;

public interface SerializeWriter {

    void writeSerialize(Object obj) throws IOException, SerializationException;

}
