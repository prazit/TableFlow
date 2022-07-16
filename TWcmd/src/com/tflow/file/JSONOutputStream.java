package com.tflow.file;

import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.mapstruct.factory.Mappers;

import java.io.*;

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
                jsonData.setDataClass(data.getClass().getName());
                jsonData.setData(data);
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
