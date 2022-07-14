package com.tflow.kafka;

import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.JavaRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Java;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ObjectDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        Object object;
        try {
            object = SerializeUtil.deserialize(data);
        } catch (IOException | ClassNotFoundException ex) {
            LoggerFactory.getLogger(ObjectDeserializer.class).error("", ex);
            throw new SerializationException(": ", ex);
        }

        /* for Request Consumer */
        if (!(object instanceof JavaRecordData)) {
            return object;
        }

        /* for Write Consumer, for Read Consumer*/
        try {
            JavaRecordData javaRecordData = (JavaRecordData) object;
            Object dataObject = SerializeUtil.deserialize(javaRecordData.getData());
            return new RecordData(dataObject, javaRecordData.getAdditional());
        } catch (Error | Exception ex) {
            LoggerFactory.getLogger(JSONDeserializer.class).error("", ex);
            throw new SerializationException(ex.getMessage(), ex);
        }


    }

}
