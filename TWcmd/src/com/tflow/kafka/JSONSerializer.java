package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class JSONSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object data) {
        if (data == null)
            return null;

        /* for Read Producer */
        if (data instanceof byte[]) {
            return (byte[]) data;
        }

        /* for Write Producer, for Read Producer */
        byte[] serialized;
        if (data instanceof KafkaRecord) {
            try {
                KafkaRecord kafkaRecord = (KafkaRecord) data;

                JSONKafkaRecord jsonKafkaRecord = new JSONKafkaRecord();
                jsonKafkaRecord.setData(SerializeUtil.toTJsonString(kafkaRecord.getData()));
                jsonKafkaRecord.setAdditional(kafkaRecord.getAdditional());

                serialized = SerializeUtil.toTJson(jsonKafkaRecord);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        /* for Request Producer, for Commit function */
        else {
            try {
                serialized = SerializeUtil.toTJson(data);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        /*LoggerFactory.getLogger(JSONSerializer.class).warn("JSONSerialize: serialized={}", new String(serialized, StandardCharsets.ISO_8859_1));*/
        return serialized;
    }
}
