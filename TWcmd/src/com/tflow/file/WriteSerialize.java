package com.tflow.file;

import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;

public interface WriteSerialize {

    public void writeSerialize(Object obj) throws IOException, SerializationException;

}
