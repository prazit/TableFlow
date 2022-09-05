package com.tflow.file;

import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class JSONOutputStream extends DataOutputStream implements SerializeWriter {

    public JSONOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public void writeSerialize(Object obj) throws IOException, SerializationException {

        /* for Write Consumer */
        if (obj instanceof RecordData) {
            RecordMapper mapper = Mappers.getMapper(RecordMapper.class);
            try {
                RecordData recordData = (RecordData) obj;
                JSONRecordData jsonData = new JSONRecordData();
                Object data = recordData.getData();

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
                    }else{
                        jsonData.setDataClass(arrayList.get(0).getClass().getName());
                    }
                    jsonData.setData(data);

                } else {
                    jsonData.setDataClass(data.getClass().getName());
                    jsonData.setData(data);
                }

                jsonData.setAdditional(recordData.getAdditional());
                write(SerializeUtil.toTJson(jsonData));
            } catch (Error | Exception ex) {
                throw new SerializationException(": ", ex);
            }
            return;
        }

        /* for Request Consumer */
        write(SerializeUtil.toTJson(obj));

    }
}
