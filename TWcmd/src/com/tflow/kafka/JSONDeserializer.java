package com.tflow.kafka;

import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JSONDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        /* for Read Consumer */
        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        LoggerFactory.getLogger(JSONDeserializer.class).warn("JSONDeserialize: received={}", new String(data, StandardCharsets.ISO_8859_1));
        Object object = null;
        try {
            object = SerializeUtil.fromTJson(data);
        } catch (Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }

        /* for Request Consumer */
        if (!(object instanceof JSONKafkaRecord)) {
            return object;
        }

        /* for Write Consumer, for Read Consumer*/
        RecordMapper mapper = Mappers.getMapper(RecordMapper.class);
        try {
            JSONKafkaRecord jsonRecordData = (JSONKafkaRecord) object;
            Object dataObject = SerializeUtil.fromTJsonString(jsonRecordData.getData());
            LoggerFactory.getLogger(JSONDeserializer.class).warn("JSONDeserialize: deserialized-object={}", dataObject.getClass().getName());
            return new KafkaRecord(dataObject, jsonRecordData.getAdditional());
        } catch (Error | Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }

}
