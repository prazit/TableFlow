package com.tflow.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tflow.file.JSONInputStream;
import com.tflow.file.JSONOutputStream;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.data.FileType;
import com.tflow.model.data.PackageData;
import com.tflow.model.data.PackageFileData;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.MessageFormatter;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonUT {

    private Gson gson;

    String indent = "";
    String indentChars = "\t";

    void println(String string) {
        System.out.println(indent + string);
    }

    void indent() {
        indent(1);
    }

    void indent(int addIndent) {
        if (addIndent > 0) {
            StringBuilder builder = new StringBuilder(indent);
            for (; addIndent > 0; addIndent--) builder.append(indentChars);
            indent = builder.toString();
            return;
        }
        // addIndex < 0
        int remove = Math.abs(addIndent) * indentChars.length();
        if (remove > indent.length()) {
            indent = "";
        } else {
            indent = indent.substring(0, indent.length() - remove);
        }
    }

    @BeforeEach
    public void initGson() {
        if (gson == null)
            gson = new GsonBuilder()
                    .setDateFormat("dd/MM/yyyy HH:mm:ss.SSSZ")
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                    .setPrettyPrinting()
                    .create();
    }

    private PackageData createPackageData() {
        PackageData packageData = new PackageData();
        packageData.setId(1);
        packageData.setName("Package");
        packageData.setBuildDate(DateTimeUtil.now());
        packageData.setBuiltDate(DateTimeUtil.getDatePlusHoursAndMinutes(packageData.getBuildDate(), 0, 2));
        packageData.setComplete(100);
        packageData.setFinished(true);
        packageData.setLastFileId(3);

        ArrayList<PackageFileData> fileList = new ArrayList<>();
        packageData.setFileList(fileList);
        fileList.add(newPackageFileData(1, "build/js/", "test.js"));
        fileList.add(newPackageFileData(2, "build/css/", "test.css"));
        fileList.add(newPackageFileData(3, "build/images/", "test.png"));

        return packageData;
    }

    @Test
    public void searchReplaceRegex() {
        String testcase = "This;is/name after+-=replaced_with-very-long-name";
        String search = "[\\p{Punct}\\s]";
        String replace = "_";
        String replaced = testcase.replaceAll(search, replace);
        String improved = replaced.replaceAll("_+", replace).toLowerCase();

        println(MessageFormatter.format("search: '{}'", search).getMessage());
        println("replace: ''");
        indent();

        println(MessageFormatter.format("source: '{}'", testcase).getMessage());
        println(MessageFormatter.format("replaced: '{}'", replaced).getMessage());
        println(MessageFormatter.format("improved: '{}'", improved).getMessage());
    }

    @Test
    public void testSerializeTJson() {

        PackageData packageData = createPackageData();
        println("BEFORE: " + packageData);

        String json = SerializeUtil.toTJsonString(packageData);
        println("JSON: " + json);

        try {
            packageData = (PackageData) SerializeUtil.fromTJsonString(json);
        } catch (Exception e) {
            println("ERROR: " + e.getMessage());
        }
        println("AFTER: " + packageData);

    }

    protected OutputStream createOutputStream(String className, FileOutputStream fileOutputStream) throws InstantiationException {
        try {
            Class outputClass = Class.forName(className);
            Constructor constructor = outputClass.getDeclaredConstructor(OutputStream.class);
            return (OutputStream) constructor.newInstance(fileOutputStream);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new InstantiationException(className + " creation failed, " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testJSonOutputStream() throws InstantiationException, IOException, ClassNotFoundException {

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

        println("---- testJSonOutputStream ----");

        List<PackageData> testData = new ArrayList<PackageData>(Arrays.asList(createPackageData(), createPackageData()));
        //List<PackageData> testData = new ArrayList<PackageData>();
        //List<Integer> testData = new ArrayList<>(Arrays.asList(12, 34, 56, 78, 90));
        //List<Integer> testData = new ArrayList<>();

        RecordData recordData = new RecordData();
        recordData.setData(testData);
        recordData.setAdditional(createAdditional());
        printRecordData("BEFORE: ", recordData);

        ByteArrayOutputStream jsonOutputSteam = new ByteArrayOutputStream();
        JSONOutputStream outputStream = new JSONOutputStream(jsonOutputSteam);
        outputStream.writeSerialize(recordData);
        outputStream.close();

        String json = new String(jsonOutputSteam.toByteArray(), StandardCharsets.ISO_8859_1);
        println("JSON: " + json);

        /*-- normal cast to known object --*/
        ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.ISO_8859_1));
        JSONInputStream inputStream = new JSONInputStream(jsonInputStream);
        recordData = (RecordData) inputStream.readSerialize();
        inputStream.close();

        printRecordData("AFTER: ", recordData);
        println("---- testJSonOutputStream ----");
    }

    private void printRecordData(String arg, RecordData recordData) {
        println(arg + recordData);
        {
            indent();
            println("data-type: " + recordData.getData().getClass().getName());
            if (recordData.getData() instanceof ArrayList) {
                ArrayList arrayList = (ArrayList) recordData.getData();
                println("list-size: " + arrayList.size());
                if (arrayList.size() > 0) {
                    println("list-item-type: " + arrayList.get(0).getClass().getName());
                } else {
                    println("list-item-type: Object");
                }
            }
            indent(-1);
        }
    }

    private RecordAttributesData createAdditional() {
        RecordAttributesData additional = new RecordAttributesData();
        additional.setProjectId("P80");
        additional.setCreatedDate(DateTimeUtil.now());
        additional.setCreatedUserId(1);
        additional.setCreatedClientId(1);
        additional.setModifiedDate(DateTimeUtil.now());
        additional.setModifiedUserId(1);
        additional.setModifiedClientId(1);
        return additional;
    }

    private PackageFileData newPackageFileData(int id, String path, String name) {
        PackageFileData packageFileData = new PackageFileData();
        packageFileData.setId(id);
        packageFileData.setName(name);
        packageFileData.setExt(FileNameExtension.forName(name));
        packageFileData.setType(FileType.GENERATED);
        packageFileData.setFileId(id);
        packageFileData.setBuildPath(path);
        packageFileData.setUpdated(true);
        return packageFileData;
    }

}
