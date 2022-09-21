package com.tflow.kafka;

import com.google.gson.Gson;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

public class JSONDeserializer implements Deserializer<Object> {

    public Object deserialize(String topic, byte[] data) {
        if (data == null)
            return null;

        /* for Read Consumer */
        if (data.length == 16) {
            return SerializeUtil.deserializeHeader(data);
        }

        /*LoggerFactory.getLogger(JSONDeserializer.class).warn("JSONDeserialize: received={}", new String(data, StandardCharsets.ISO_8859_1));*/
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
            Class dataClass = Class.forName(jsonRecordData.getDataClass());
            Object dataObject = jsonRecordData.getData();
            Gson gson = SerializeUtil.getGson();
            if (dataObject == null) {
                /*ignored*/

            } else if (dataObject instanceof ArrayList) {
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
                String dataJson;
                Object[] objects = (Object[]) ((ArrayList) dataObject).toArray();
                ArrayList<Object> arrayList = new ArrayList<>();
                if (objects.length > 0) {
                    for (Object obj : objects) {
                        dataJson = gson.toJson(obj);
                        arrayList.add(gson.fromJson(dataJson, dataClass));
                    }
                }
                dataObject = arrayList;

                /*need to change List<Double> to List<Integer>*/
                if (Integer.class.getName().compareTo(dataClass.getName()) == 0) {
                    List list = (List) dataObject;
                    if (list.size() > 0 && list.get(0) instanceof Double) {
                        dataObject = toIntegerList(list);
                    }
                }

            } else {
                String dataJson = gson.toJson(dataObject);
                dataObject = gson.fromJson(dataJson, dataClass);
            }

            return new KafkaRecord(dataObject, jsonRecordData.getAdditional());
        } catch (Error | Exception ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }

    private Object toIntegerList(List<Double> list) {
        List<Integer> integerList = new ArrayList<>();
        for (Double aDouble : list) {
            integerList.add(aDouble.intValue());
        }
        return integerList;
    }

}
