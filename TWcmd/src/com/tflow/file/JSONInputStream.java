package com.tflow.file;

import com.google.gson.Gson;
import com.tflow.model.data.record.JSONRecordData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.SerializeUtil;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JSONInputStream extends DataInputStream implements SerializeReader {

    public JSONInputStream(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Copied from JDK 11.0
     */
    public byte[] readAllBytes() throws IOException {
        return this.readNBytes(2147483647);
    }

    /**
     * Copied from JDK 11.0
     */
    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        } else {
            List<byte[]> bufs = null;
            byte[] result = null;
            int total = 0;
            int remaining = len;

            int n;
            do {
                byte[] buf = new byte[Math.min(remaining, 8192)];

                int nread;
                for (nread = 0; (n = this.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0; remaining -= n) {
                    nread += n;
                }

                if (nread > 0) {
                    if (2147483639 - total < nread) {
                        throw new OutOfMemoryError("Required array size too large");
                    }

                    total += nread;
                    if (result == null) {
                        result = buf;
                    } else {
                        if (bufs == null) {
                            bufs = new ArrayList();
                            bufs.add(result);
                        }

                        bufs.add(buf);
                    }
                }
            } while (n >= 0 && remaining > 0);

            if (bufs == null) {
                if (result == null) {
                    return new byte[0];
                } else {
                    return result.length == total ? result : Arrays.copyOf(result, total);
                }
            } else {
                result = new byte[total];
                int offset = 0;
                remaining = total;

                int count;
                for (Iterator var12 = bufs.iterator(); var12.hasNext(); remaining -= count) {
                    byte[] b = (byte[]) var12.next();
                    count = Math.min(b.length, remaining);
                    System.arraycopy(b, 0, result, offset, count);
                    offset += count;
                }

                return result;
            }
        }
    }

    @Override
    public Object readSerialize() throws IOException, ClassNotFoundException {
        Object object = null;
        try {
            object = SerializeUtil.fromTJson(readAllBytes());
        } catch (Exception ex) {
            throw new IOException("Deserialize from TJson failed, ", ex);
        }

        /* for Request Consumer */
        if (!(object instanceof JSONRecordData)) {
            return object;
        }

        /* for Write Consumer */
        JSONRecordData jsonRecordData = (JSONRecordData) object;
        Class dataClass = Class.forName(jsonRecordData.getDataClass());
        Gson gson = SerializeUtil.getGson();
        try {
            Object dataObject = jsonRecordData.getData();
            if (dataObject instanceof ArrayList) {
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


            RecordData recordData = new RecordData();
            recordData.setData(dataObject);
            recordData.setAdditional(jsonRecordData.getAdditional());

            return recordData;
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
