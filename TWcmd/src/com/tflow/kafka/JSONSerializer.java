package com.tflow.kafka;

import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.ArrayList;

public class JSONSerializer implements Serializer<Object> {
    public byte[] serialize(String topic, Object obj) {
        if (obj == null)
            return null;

        /* for Read Producer */
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }

        /* for Write Producer, for Read Producer */
        byte[] serialized;
        if (obj instanceof KafkaRecord) {
            try {
                KafkaRecord kafkaRecord = (KafkaRecord) obj;
                JSONKafkaRecord jsonData = new JSONKafkaRecord();
                Object data = kafkaRecord.getData();

                if (data instanceof ArrayList) {
                    /* JSON Data Formatted File Problems
                        1. ArrayList problem: read from file, data-type has changed to LinkedTreeMap
                        2. ArrayList problem: write to file, unable to check data type of object in the list when list is empty

                        Solution:
                        1. Write to file (JSONOutputSteam) need to transform ArrayList to Array of Object in two cases below
                           + EmptyList to Empty Array of Object, dataClass=java.lang.Object
                           + NotEmmptyList to Array of KnownObject, dataClass=KnowObject
                        2. Read from file (JSONInputStream) need to transform Array to ArrayList
                           + when data is Array
                           + create empty ArrayList
                           + case: data-array.length > 0, convert from JSON using dataClass item by item
                     **/
                    ArrayList arrayList = (ArrayList) data;
                    if (arrayList.size() == 0) {
                        jsonData.setDataClass(Object.class.getName());
                    } else {
                        jsonData.setDataClass(arrayList.get(0).getClass().getName());
                    }
                    jsonData.setData(data);

                } else {
                    jsonData.setDataClass(data.getClass().getName());
                    jsonData.setData(data);
                }

                jsonData.setAdditional(kafkaRecord.getAdditional());
                serialized = SerializeUtil.toTJson(jsonData);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        /* for Request Producer, for Commit function */
        else {
            try {
                serialized = SerializeUtil.toTJson(obj);
            } catch (Exception ex) {
                throw new SerializationException(": ", ex);
            }
        }

        /*LoggerFactory.getLogger(JSONSerializer.class).warn("JSONSerialize: serialized={}", new String(serialized, StandardCharsets.ISO_8859_1));*/
        return serialized;
    }
}
