package com.tflow.tbcmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.Pair;
import com.clevel.dconvers.transform.TransformTypes;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.DConversID;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FileUtil;
import com.tflow.wcmd.IOCommand;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Write-Data need to use TWcmd to support backup-site/rerun in any case of data-loss.
 * Read-Data don't need to use TRcmd.
 */
public class BuildPackageCommand extends IOCommand {

    private Logger log = LoggerFactory.getLogger(BuildPackageCommand.class);

    private KafkaRecordAttributes attributes;
    private RecordAttributesData recordAttributes;

    private DataManager dataManager;
    private PackageMapper mapper;

    private String generatedPath;
    private DConvers dconvers;

    public BuildPackageCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs, DataManager dataManager) {
        super(offset, key, value, environmentConfigs);
        this.dataManager = dataManager;
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    /**
     * Notice: key is command, now have only 1 'build' command
     * Notice: first version assume ProjectType always be BATCH
     */
    @Override
    public void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException {
        mapper = Mappers.getMapper(PackageMapper.class);
        attributes = (KafkaRecordAttributes) value;
        recordAttributes = Mappers.getMapper(RecordMapper.class).map(attributes);

        ProjectData projectData = null;
        List<ItemData> packageList = null;
        PackageData packageData = null;
        List<PackageFileData> fileList = new ArrayList<>();
        ProjectUser projectUser = mapper.map(attributes);

        try {
            Object data = getData(ProjectFileType.PROJECT, projectUser.getId());
            projectData = (ProjectData) throwExceptionOnError(data);

            data = getData(ProjectFileType.PACKAGE_LIST);
            packageList = (List<ItemData>) throwExceptionOnError(data);

            boolean isNewPackage = attributes.getRecordId() == null;
            int packageId;
            if (isNewPackage) {
                if (packageList.size() == 0) {
                    packageId = 1;
                } else {
                    packageList.sort((s, t) -> Integer.compare(t.getId(), s.getId()));
                    packageId = packageList.get(0).getId() + 1;
                }

                packageData = new PackageData();
                packageData.setId(packageId);
            } else {
                packageId = Integer.parseInt(attributes.getRecordId());
                data = getData(ProjectFileType.PACKAGE, packageId);
                packageData = (PackageData) throwExceptionOnError(data);
                packageData.setComplete(0);
                packageData.setFinished(false);
                packageData.setFileList(null);
                packageData.setBuiltDate(null);
            }

            packageData.setType(projectData.getType().getPackageType());
            packageData.setBuildDate(DateTimeUtil.now());
            packageData.setName("building...");

            ItemData packageItemData = mapper.map(packageData);
            if (isNewPackage) {
                packageList.add(packageItemData);
            } else {
                setPackageNameInList(packageId, packageData.getName(), packageList);
            }

            buildPackage(packageData, fileList, packageList, projectUser, projectData);

        } catch (Exception exception) {
            if (packageList != null) {
                /*need to savePackage before Throw Exception for Rejected*/
                if (packageData == null) packageData = new PackageData();
                packageData.setName(exception.getMessage());
                packageData.setFileList(fileList);
                packageData.setFinished(true);
                updatePackageList(packageData, packageList, projectUser);
                updatePercentComplete(packageData, projectUser, packageData.getComplete(), DateTimeUtil.now());
            } else {
                log.error("Package List not found on project({}), package will not created!", projectUser.getId());
            }

            throw exception;
        }
    }

    private void setPackageNameInList(int packageId, String newName, List<ItemData> packageList) {
        for (ItemData item : packageList) {
            if (item.getId() == packageId) {
                item.setName(newName);
                break;
            }
        }
    }

    /**
     * @param packageData contains percent complete for ui, this function will update them 4-5 times max
     */
    @SuppressWarnings("unchecked")
    private void buildPackage(PackageData packageData, List<PackageFileData> fileList, List<ItemData> packageList, ProjectUser projectUser, ProjectData projectData) throws InstantiationException, IOException, ClassNotFoundException {
        setPackageNameInList(packageData.getId(), packageData.getName(), packageList);
        updatePackageList(packageData, packageList, projectUser);
        updatePercentComplete(packageData, projectUser, 0, estimateBuiltDate());

        addUploadedFiles(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 25, estimateBuiltDate());

        addVersionedFiles(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 50, estimateBuiltDate());

        addGeneratedFiles(fileList, packageData, projectUser, projectData);
        updatePercentComplete(packageData, projectUser, 75, estimateBuiltDate());

        String completeName = getCompleteName(packageData, projectData);
        packageData.setName(completeName);
        packageData.setFileList(fileList);
        packageData.setFinished(true);
        fileList.sort(Comparator.comparing(item -> (item.getBuildPath() + item.getName())));

        /*TODO: need to compare previous version and mark for New/Updated File*/

        setPackageNameInList(packageData.getId(), packageData.getName(), packageList);
        updatePackageList(packageData, packageList, projectUser);
        updatePercentComplete(packageData, projectUser, 100, DateTimeUtil.now());
    }

    private void updatePackageList(PackageData packageData, List<ItemData> packageList, ProjectUser projectUser) {
        ItemData itemData = packageList.get(packageList.size() - 1);
        itemData.setName(packageData.getName());
        KafkaRecordAttributes attributes = dataManager.addData(ProjectFileType.PACKAGE_LIST, packageList, projectUser);
    }

    private Object getData(ProjectFileType projectFileType, RecordAttributesData recordAttributesData) throws InstantiationException, IOException, ClassNotFoundException {
        File file = getFile(projectFileType, recordAttributesData);
        if (!file.exists()) {
            return KafkaErrorCode.DATA_FILE_NOT_FOUND.getCode();
        }

        try {
            RecordData recordData = (RecordData) readFrom(file);
            return recordData.getData();
        } catch (ClassCastException ex) {
            return KafkaErrorCode.INVALID_DATA_FILE.getCode();
        }
    }

    private Object getData(ProjectFileType projectFileType) throws InstantiationException, IOException, ClassNotFoundException {
        return getData(projectFileType, recordAttributes);
    }

    private Object getData(ProjectFileType projectFileType, String recordId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(recordId);
        return getData(projectFileType, recordAttributesData);
    }

    private Object getData(ProjectFileType projectFileType, int recordId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        return getData(projectFileType, recordAttributesData);
    }

    private Object getData(ProjectFileType projectFileType, int recordId, int stepId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        return getData(projectFileType, recordAttributesData);
    }

    private Object getData(ProjectFileType projectFileType, int recordId, int stepId, int dataTableId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        recordAttributesData.setDataTableId(String.valueOf(dataTableId));
        return getData(projectFileType, recordAttributesData);
    }

    private Object getData(ProjectFileType projectFileType, int recordId, int stepId, int ignoredId, int transformTableId) throws ClassNotFoundException, IOException, InstantiationException {
        RecordAttributesData recordAttributesData = mapper.clone(recordAttributes);
        recordAttributesData.setRecordId(String.valueOf(recordId));
        recordAttributesData.setStepId(String.valueOf(stepId));
        recordAttributesData.setTransformTableId(String.valueOf(transformTableId));
        return getData(projectFileType, recordAttributesData);
    }

    private String getCompleteName(PackageData packageData, ProjectData projectData) {
        int id = packageData.getId();
        String version = projectData.getVersion();
        return new DConversID(projectData.getName() + ((version == null) ? ("_r" + id) : ("_" + version + "." + id))).toString();
    }

    private void updatePercentComplete(PackageData packageData, ProjectUser projectUser, int percent, Date builtDate) {
        packageData.setComplete(percent);
        packageData.setBuiltDate(builtDate);
        dataManager.addData(ProjectFileType.PACKAGE, packageData, projectUser, packageData.getId());
        dataManager.waitAllTasks();
    }

    @SuppressWarnings("unchecked")
    private void addVersionedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) throws IOException, ClassNotFoundException, InstantiationException {
        /*TODO: future feature: need real filter from Project.Type*/
        String filter = ProjectType.BATCH.getCode();
        Object data = getData(ProjectFileType.VERSIONED_LIST);
        List<VersionedFileData> versionedFileDataList = (List<VersionedFileData>) throwExceptionOnError(data);
        Versioned versioned;
        for (VersionedFileData versionedFileData : versionedFileDataList) {
            versioned = Versioned.valueOf(versionedFileData.getId());
            if (versioned.getProjectTypeCodes().contains(filter)) {
                PackageFileData packageFileData = new PackageFileData();
                packageFileData.setType(FileType.VERSIONED);
                packageFileData.setId(newPackageFileId(packageData));
                packageFileData.setFileId(versioned.getFileId());
                packageFileData.setName(versionedFileData.getName());
                packageFileData.setBuildDate(packageData.getBuildDate());
                FileNameExtension fileNameExtension = FileNameExtension.forName(versionedFileData.getName());
                packageFileData.setExt(fileNameExtension);
                packageFileData.setBuildPath(fileNameExtension.getBuildPath());
                fileList.add(packageFileData);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addUploadedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) throws IOException, ClassNotFoundException, InstantiationException {
        Object data = getData(ProjectFileType.UPLOADED_LIST);
        List<BinaryFileItemData> binaryFileItemDataList = (List<BinaryFileItemData>) throwExceptionOnError(data);
        for (BinaryFileItemData binaryFileItemData : binaryFileItemDataList) {
            PackageFileData packageFileData = new PackageFileData();
            packageFileData.setType(FileType.UPLOADED);
            packageFileData.setId(newPackageFileId(packageData));
            packageFileData.setFileId(binaryFileItemData.getId());
            packageFileData.setName(binaryFileItemData.getName());
            packageFileData.setBuildDate(packageData.getBuildDate());
            FileNameExtension fileNameExtension = FileNameExtension.forName(binaryFileItemData.getName());
            packageFileData.setExt(fileNameExtension);
            packageFileData.setBuildPath(fileNameExtension.getBuildPath());
            fileList.add(packageFileData);
        }
    }

    private int newPackageFileId(PackageData packageData) {
        int packageFileId = packageData.getLastFileId() + 1;
        packageData.setLastFileId(packageFileId);
        return packageFileId;
    }

    private Date estimateBuiltDate() {
        return DateTimeUtil.now();
    }

    private Object throwExceptionOnError(Object data) throws IOException {
        if (data instanceof Long) {
            throw new IOException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

    private void addGeneratedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser, ProjectData projectData) throws IOException, UnsupportedOperationException, InstantiationException, ClassNotFoundException {
        generatedPath = environmentConfigs.getBinaryRootPath() + projectUser.getId() + "/";

        String conversionFileName = new DConversID(projectData.getName()).toString() + Defaults.CONFIG_FILE_EXT.getStringValue();
        conversionFileName = conversionFileName.toLowerCase();
        createEmptyFile(generatedPath + conversionFileName);

        dconvers = new DConvers(new String[]{
                "--library-mode=manual"
                /*, "--source-type=" + ConfigFileTypes.PROPERTIES.name()*/
                , "--source=" + generatedPath + conversionFileName
                /*,"--save-default-value"*/
        });
        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;

        try {
            List<DatabaseData> databaseDataList = loadDatabaseDataList();
            List<SFTPData> sftpDataList = new ArrayList<>();
            List<LocalData> localDataList = loadLocalDataList(projectUser);

            Object data = getData(ProjectFileType.GENERATED_LIST);
            List<ItemData> generatedFileList = (List<ItemData>) throwExceptionOnError(data);

            ByteArrayOutputStream byteArrayOutputStream;
            ItemData generatedItemData;
            byte[] contentBytes;

            String converterFileName;
            List<Integer> usedDatabaseIdList = new ArrayList<>();
            List<Integer> usedSFTPIdList = new ArrayList<>();
            HashMap<String, ConverterConfigFile> converterConfigMap = getConverterConfigMap(projectUser, fileList, databaseDataList, sftpDataList, localDataList, usedDatabaseIdList, usedSFTPIdList);
            for (ConverterConfigFile converterConfigFile : converterConfigMap.values()) {
                converterFileName = extractFileName(converterConfigFile.getName());

                /*create Generated Converter File*/
                byteArrayOutputStream = new ByteArrayOutputStream();
                converterConfigFile.saveProperties(byteArrayOutputStream);
                contentBytes = byteArrayOutputStream.toByteArray();
                log.debug("Converter:saveProperties successful, {}\n{}", converterFileName, new String(contentBytes, StandardCharsets.ISO_8859_1));

                generatedItemData = new ItemData(generatedFileList.size() + 1, converterFileName);
                generatedFileList.add(generatedItemData);
                addGeneratedFile(generatedItemData.getId(), converterFileName, false, contentBytes, packageData, projectUser, fileList);
            }

            /*remove unused database before create ConversionConfigFile*/
            List<DatabaseData> usedDatabaseDataList = new ArrayList<>();
            for (Integer databaseId : usedDatabaseIdList) {
                usedDatabaseDataList.add(findDatabaseData(databaseId, databaseDataList));
            }
            databaseDataList = usedDatabaseDataList;

            /*remove unused sftp before create ConversionConfigFile*/
            List<SFTPData> usedSFTPDataList = new ArrayList<>();
            for (Integer sftpId : usedSFTPIdList) {
                usedSFTPDataList.add(findSftpData(sftpId, sftpDataList));
            }
            sftpDataList = usedSFTPDataList;
            if (log.isDebugEnabled()) {
                log.debug("usedSFTPDataList: {}", Arrays.toString(usedSFTPDataList.toArray()));
                log.debug("sftpDataList: {}", Arrays.toString(sftpDataList.toArray()));
            }

            addDataConversionConfigFile(conversionFileName, dataConversionConfigFile, converterConfigMap, packageData, projectUser, fileList, databaseDataList, sftpDataList, generatedFileList);
            addGeneratedBatchFiles(conversionFileName, projectData, packageData, projectUser, fileList, generatedFileList);
            addGeneratedReadme(dconvers, projectData, packageData, projectUser, fileList, generatedFileList);

            /*GENERATED_LIST need to save at the end*/
            dataManager.addData(ProjectFileType.GENERATED_LIST, generatedFileList, projectUser);

            log.info("generate dconvers-config-files success.\n");
        } catch (Exception ex) {
            log.error("generate dconvers-config-files error: " + ex.getMessage());
            log.trace("", ex);
            throw new IOException("generate file failed: ", ex);
        }
    }

    private void addGeneratedReadme(DConvers dconvers, ProjectData projectData, PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList, List<ItemData> generatedFileList) {

        String fileName = "version.properties";
        String[] numbers = projectData.getVersion().split("[.]");
        String versionName = "version";
        int versionNumber = 0;
        int revisionNumber = 0;
        if (numbers.length > 2) {
            StringBuilder numberBuilder = new StringBuilder();
            int last = numbers.length - 2;
            for (int i = 0; i < last; i++) numberBuilder.append(".").append(numbers[i]);
            versionName += " " + numberBuilder.substring(1);
            if (numbers.length > 3) versionNumber = Integer.parseInt(numbers[3]);
            if (numbers.length > 4) revisionNumber = Integer.parseInt(numbers[4]);
        } else {
            if (numbers.length > 0) versionNumber = Integer.parseInt(numbers[0]);
            if (numbers.length > 1) revisionNumber = Integer.parseInt(numbers[1]);
        }
        String content = "project.name=" + projectData.getName() + "\n" +
                "version.name=" + versionName + "\n" +
                "version.number=" + versionNumber + "\n" +
                "revision.number=" + revisionNumber + "\n" +
                "build.number=" + packageData.getId() + "\n" +
                "build.date=" + DateTimeUtil.getDateStr(packageData.getBuildDate(), "dd/MM/yyyy HH:mm") + "\n" +
                "";
        byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
        ItemData generatedItemData = new ItemData(generatedFileList.size() + 1, fileName);
        generatedFileList.add(generatedItemData);
        addGeneratedFile(generatedItemData.getId(), fileName, true, contentBytes, packageData, projectUser, fileList);

        /*collect file list for readme.txt*/
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (PackageFileData file : fileList) {
            count++;
            stringBuilder
                    .append(" |_ ").append(count).append(") ")
                    .append(file.getBuildPath()).append(file.getName()).append("\n");
        }

        fileName = "readme.txt";
        stringBuilder
                .append(" |_ ").append(++count).append(") ")
                .append(FileNameExtension.forName(fileName).getBuildPath()).append(fileName).append("\n");

        content = projectData.getName() + " version " + projectData.getVersion() + " R" + packageData.getId() + "\n" +
                "Generated by TFLOW DEMO VERSION\n" +
                "on " + dconvers.getSystemVariableValue(SystemVariable.APPLICATION_FULL_VERSION) + "\n" +
                "\n" +
                "Packaged (original-name): " + getCompleteName(packageData, projectData) + "." + packageData.getType().name().toLowerCase() + "\n" +
                stringBuilder.toString();

        contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
        generatedItemData = new ItemData(generatedFileList.size() + 1, fileName);
        generatedFileList.add(generatedItemData);
        addGeneratedFile(generatedItemData.getId(), fileName, true, contentBytes, packageData, projectUser, fileList);
    }

    private BinaryFileData addGeneratedFile(int fileId, String fileName, boolean rootPath, byte[] contentBytes, PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList) {
        BinaryFileData conversionFileData = new BinaryFileData();
        conversionFileData.setId(fileId);
        conversionFileData.setName(fileName);
        conversionFileData.setExt(FileNameExtension.forName(fileName));
        conversionFileData.setContent(contentBytes);
        dataManager.addData(ProjectFileType.GENERATED, conversionFileData, projectUser, conversionFileData.getId());

        PackageFileData packageFileData = mapper.map(conversionFileData);
        packageFileData.setId(newPackageFileId(packageData));
        packageFileData.setType(FileType.GENERATED);
        packageFileData.setBuildPath(rootPath ? "" : packageFileData.getExt().getBuildPath());
        fileList.add(packageFileData);
        return conversionFileData;
    }

    private void addGeneratedBatchFiles(String conversionFileName, ProjectData projectData, PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList, List<ItemData> generatedFileList) throws InstantiationException, IOException, ClassNotFoundException {
        FileNameExtension ext = FileNameExtension.forName(conversionFileName);

        /*TODO: future feature: need Batch Option object later*/
        boolean generateBatchFile = true;
        boolean generateShFile = true;
        String javaHome = "";
        String logLevel = "INFO";
        String logFileName = "/Apps/DConvers/Logs/$[conversionFileName]";

        /*generate batcch script*/
        String clsPath = getJavaClassPath(fileList);
        String source = ext.getBuildPath() + conversionFileName;
        String logback = "logback.xml";
        String content;
        String fileName;
        ItemData generatedItemData;
        byte[] contentBytes;
        Charset iso88591 = StandardCharsets.ISO_8859_1;
        if (generateBatchFile) {
            String batTemplate = "" +
                    "@set JAVA_BIN={}\n" +
                    "@set SOURCEPATH={}\n" +
                    "@set LOGBACKPATH={}\n" +
                    "@set LEVEL={}\n" +
                    "@set CLSPATH={}\n" +
                    "@\"%JAVA_BIN%java.exe\" -Xms64m -Xmx2g -Dfile.encoding=UTF-8 -Duser.timezone=\"GMT+7\" -Duser.language=en -Duser.region=EN -Duser.country=US --class-path \"%CLSPATH%\" com.clevel.dconvers.Main --source=\"%SOURCEPATH%\" --logback=\"%LOGBACKPATH%\" --level=%LEVEL%\n";
            content = MessageFormatter.arrayFormat(batTemplate, new Object[]{javaHome, source, FileNameExtension.XML.getBuildPath() + logback, logLevel, clsPath}).getMessage();
            content = content.replaceAll("[/]", "\\\\");
            contentBytes = content.getBytes(iso88591);

            fileName = "run-" + new DConversID(projectData.getName()) + ".bat";
            generatedItemData = new ItemData(generatedFileList.size() + 1, fileName);
            generatedFileList.add(generatedItemData);
            addGeneratedFile(generatedItemData.getId(), fileName, true, contentBytes, packageData, projectUser, fileList);
        }

        /*generate shell script*/
        if (generateShFile) {
            javaHome = "";
            String shTemplate = "" +
                    "JAVA_BIN={}\n" +
                    "SOURCE={}\n" +
                    "LEVEL={}\n" +
                    "CLSPATH={}\n" +
                    "\"${JAVA_BIN}java\" -Xms64m -Xmx2g -Dfile.encoding=UTF-8 -Duser.timezone=\"GMT+7\" -Duser.language=en -Duser.region=EN -Duser.country=US --class-path \"${CLSPATH}\" com.clevel.dconvers.Main --source=\"${SOURCE}\" --level=${LEVEL}\n";
            content = MessageFormatter.arrayFormat(shTemplate, new Object[]{javaHome, source, logLevel, clsPath}).getMessage();
            contentBytes = content.getBytes(iso88591);

            fileName = "run-" + new DConversID(projectData.getName()) + ".sh";
            generatedItemData = new ItemData(generatedFileList.size() + 1, fileName);
            generatedFileList.add(generatedItemData);
            addGeneratedFile(generatedItemData.getId(), fileName, true, contentBytes, packageData, projectUser, fileList);
        }

        /*generate logback.xml from template*/
        Object data = getData(ProjectFileType.VERSIONED, Versioned.TEMPLATE_LOGBACK_XML.getFileId());
        BinaryFileData binaryFileData = (BinaryFileData) throwExceptionOnError(data);
        String logbackTemplate = new String(binaryFileData.getContent(), iso88591);
        logFileName = logFileName.replaceFirst("\\$\\[conversionFileName\\]", conversionFileName.split("[.]")[0]);
        logbackTemplate = logbackTemplate.replaceAll("\\$\\[logFileName\\]", logFileName);
        contentBytes = logbackTemplate.getBytes(iso88591);
        generatedItemData = new ItemData(generatedFileList.size() + 1, logback);
        generatedFileList.add(generatedItemData);
        addGeneratedFile(generatedItemData.getId(), logback, false, contentBytes, packageData, projectUser, fileList);

        /* TODO: generate script to add job to any scheduler*/

    }

    private String getJavaClassPath(List<PackageFileData> fileList) {
        StringBuilder builder = new StringBuilder();
        for (PackageFileData fileData : fileList) {
            if (FileType.VERSIONED != fileData.getType()) continue;
            builder.append(fileData.getBuildPath()).append(fileData.getName()).append(";");
        }
        return builder.toString();
    }

    private String extractFileName(String name) {
        String[] names = name.split("[/]");
        return names[names.length - 1];
    }

    private void createEmptyFile(String fileName) {
        try {
            File file = new File(fileName);
            FileUtil.autoCreateParentDir(file);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("# empty file");
            fileWriter.close();
        } catch (IOException ex) {
            log.error("createEmptyFile(" + fileName + ") failed, ");
            log.trace("", ex);
        }
    }

    private void addDataConversionConfigFile(String fileName, DataConversionConfigFile dataConversionConfigFile, HashMap<String, ConverterConfigFile> converterConfigMap, PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList, List<DatabaseData> databaseDataList, List<SFTPData> sftpDataList, List<ItemData> generatedFileList) throws IOException, UnsupportedOperationException, ClassNotFoundException, InstantiationException, ConfigurationException {
        // all commented will use default values by DConvers

        /*dataConversionConfigFile.setPluginsCalcList();
        dataConversionConfigFile.setPluginsDataSourceList();
        dataConversionConfigFile.setPluginsOutputList();*/

        /*dataConversionConfigFile.setExitOnError(true);
        dataConversionConfigFile.setErrorCode(-1);
        dataConversionConfigFile.setSuccessCode(0);
        dataConversionConfigFile.setWarningCode(1);*/

        /*dataConversionConfigFile.setOutputSourcePath("");
        dataConversionConfigFile.setOutputMappingPath("");
        dataConversionConfigFile.setOutputTargetPath("");*/

        dataConversionConfigFile.setSourceFileNumber(1);
        dataConversionConfigFile.setMappingFileNumber(101);
        dataConversionConfigFile.setTargetFileNumber(201);

        dataConversionConfigFile.setDataSourceConfigMap(createDataSourceConfigMap(packageData, projectUser, fileList, databaseDataList));
        dataConversionConfigFile.setSftpConfigMap(createSftpConfigMap(sftpDataList));
        dataConversionConfigFile.setSmtpConfigMap(createSmtpConfigMap());

        dataConversionConfigFile.setConverterConfigMap(converterConfigMap);

        log.info("dataConversionConfigFile.saveProperties...");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        dataConversionConfigFile.saveProperties(byteArrayOutputStream);
        byte[] contentBytes = byteArrayOutputStream.toByteArray();
        log.debug("Conversion:saveProperties successful, \n{}", new String(contentBytes, StandardCharsets.ISO_8859_1));

        ItemData generatedItemData = new ItemData(generatedFileList.size() + 1, fileName);
        generatedFileList.add(generatedItemData);
        addGeneratedFile(generatedItemData.getId(), fileName, false, contentBytes, packageData, projectUser, fileList);
    }

    private List<LocalData> loadLocalDataList(ProjectUser projectUser) throws IOException {
        Object data = dataManager.getData(ProjectFileType.LOCAL_LIST, projectUser);
        return (List<LocalData>) throwExceptionOnError(data);
    }

    private List<DatabaseData> loadDatabaseDataList() throws ClassNotFoundException, IOException, InstantiationException {
        Object data = getData(ProjectFileType.DB_LIST);
        List<Integer> dbIdList = (List) throwExceptionOnError(data);

        List<DatabaseData> databaseDataList = new ArrayList<>();
        DatabaseData databaseData;
        for (Integer databaseId : dbIdList) {
            data = getData(ProjectFileType.DB, databaseId);
            databaseData = (DatabaseData) throwExceptionOnError(data);
            databaseDataList.add(databaseData);
        }
        return databaseDataList;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, DataSourceConfig> createDataSourceConfigMap(PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList, List<DatabaseData> databaseDataList) throws InstantiationException, IOException, ClassNotFoundException {
        HashMap<String, DataSourceConfig> dataSourceConfigHashMap = new HashMap<>();
        DataSourceConfig dataSourceConfig;
        for (DatabaseData databaseData : databaseDataList) {
            dataSourceConfig = getDataSourceConfig(throwExceptionOnValidateFail(databaseData, packageData, projectUser, fileList));
            dataSourceConfigHashMap.put(dataSourceConfig.getName().toUpperCase(), dataSourceConfig);
        }
        return dataSourceConfigHashMap;
    }

    private DatabaseData throwExceptionOnValidateFail(DatabaseData databaseData, PackageData packageData, ProjectUser projectUser, List<PackageFileData> fileList) throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException {
        String objectName = "Database(" + databaseData.getName() + ")";
        if (databaseData.getUrl() == null) throw newRequiredException("URL", objectName);
        if (databaseData.getDbms() == null) throw newRequiredException("DBMS", objectName);
        databaseData.setDriver(getDriver(databaseData.getDbms(), packageData, fileList));
        if (databaseData.getUser() == null) throw newRequiredException("User", objectName);
        if (databaseData.getPassword() == null) throw newRequiredException("Password", objectName);
        if (databaseData.getRetry() < 0) databaseData.setRetry(0);
        return databaseData;
    }

    private UnsupportedOperationException newRequiredException(String fieldName, String objectName) {
        return new UnsupportedOperationException(fieldName + " is required on " + objectName);
    }

    private String getDriver(String dbmsName, PackageData packageData, List<PackageFileData> fileList) throws IOException, InstantiationException, ClassNotFoundException {
        Dbms dbms = Dbms.valueOf(dbmsName);
        Versioned driverFile = dbms.getDriverFile();

        /*find existing file before*/
        boolean needAdd = true;
        int driverFileId = driverFile.getFileId();
        for (PackageFileData fileData : fileList) {
            if (fileData.getFileId() == driverFileId) {
                needAdd = false;
                break;
            }
        }

        /*add driver jar file to fileList*/
        if (needAdd) {
            Object data = getData(ProjectFileType.VERSIONED, driverFileId);
            BinaryFileData binaryFileData = (BinaryFileData) throwExceptionOnError(data);
            PackageFileData packageFileData = mapper.map(binaryFileData);
            packageFileData.setType(FileType.VERSIONED);
            packageFileData.setId(newPackageFileId(packageData));
            packageFileData.setBuildPath(packageFileData.getExt().getBuildPath());
            packageFileData.setBuildDate(packageData.getBuildDate());
            fileList.add(packageFileData);
        }

        return dbms.getDriverName();
    }

    private DataSourceConfig getDataSourceConfig(DatabaseData databaseData) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(dconvers, new DConversID(databaseData.getName()).toString());

        dataSourceConfig.setUrl(/*"jdbc:oracle:thin:@172.20.8.67:1521:FCUAT2"*/ databaseData.getUrl());
        dataSourceConfig.setDriver(/*"oracle.jdbc.driver.OracleDriver"*/databaseData.getDriver());
        dataSourceConfig.setSchema(/*"account"*/ "");
        dataSourceConfig.setUser(databaseData.getUser());
        dataSourceConfig.setPassword(databaseData.getPassword());
        dataSourceConfig.setRetry(databaseData.getRetry());

        /*-- for EmailDataSource
        dataSourceConfig.setSsl(false);
        dataSourceConfig.setHost("localhost:5210");*/

        /*dataSourceConfig.setValueQuotes("'");
        dataSourceConfig.setNameQuotes("'");*/

        dataSourceConfig.setUserEncrypted(databaseData.isUserEncrypted());
        dataSourceConfig.setPasswordEncrypted(databaseData.isPasswordEncrypted());

        List<Pair<String, String>> propList = new ArrayList<>();
        dataSourceConfig.setPropList(propList);
        for (NameValueData prop : databaseData.getPropList()) {
            propList.add(new Pair<>(prop.getName(), prop.getValue()));
        }

        /*dataSourceConfig.setPre("set system=1;");
        dataSourceConfig.setPost("commit;set system=0;");*/

        return dataSourceConfig;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, HostConfig> createSftpConfigMap(List<SFTPData> sftpDataList) {
        HashMap<String, HostConfig> sftpConfigMap = new HashMap<>();
        HostConfig hostConfig;
        for (SFTPData sftpData : sftpDataList) {
            hostConfig = getHostConfig(throwExceptionOnValidateFail(sftpData));
            sftpConfigMap.put(hostConfig.getName().toUpperCase(), hostConfig);
            sftpDataList.add(sftpData);
        }
        return sftpConfigMap;
    }

    private SFTPData throwExceptionOnValidateFail(SFTPData sftpData) {
        String objectName = "SFTP(" + sftpData.getName() + ")";
        if (sftpData.getHost() == null) throw newRequiredException("Host", objectName);
        if (sftpData.getPort() <= 0) throw newRequiredException("Port", objectName);
        if (sftpData.getTmp() == null) throw newRequiredException("Tmp", objectName);
        if (sftpData.getRootPath() == null) throw newRequiredException("RootPath", objectName);
        if (sftpData.getUser() == null) throw newRequiredException("User", objectName);
        if (sftpData.getPassword() == null) throw newRequiredException("Password", objectName);
        if (sftpData.getRetry() < 0) sftpData.setRetry(0);
        return sftpData;
    }

    private HostConfig getHostConfig(SFTPData sftpData) {
        HostConfig hostConfig = new HostConfig(dconvers, new DConversID(sftpData.getName()).toString(), Property.SFTP);

        hostConfig.setHost(sftpData.getHost());
        hostConfig.setPort(sftpData.getPort());
        hostConfig.setUser(sftpData.getUser());
        hostConfig.setPassword(sftpData.getPassword());
        hostConfig.setRetry(sftpData.getRetry());
        hostConfig.setTmp(sftpData.getTmp());

        return hostConfig;
    }

    private HashMap<String, HostConfig> createSmtpConfigMap() {
        /*TODO: Future Feature: generate smtpConfigMap*/
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, ConverterConfigFile> getConverterConfigMap(ProjectUser projectUser, List<PackageFileData> fileList, List<DatabaseData> databaseDataList, List<SFTPData> sftpDataList, List<LocalData> localDataList, List<Integer> usedDatabaseIdList, List<Integer> usedSFTPIdList) throws IOException, InstantiationException, ClassNotFoundException {
        HashMap<String, ConverterConfigFile> converterMap = new HashMap<>();

        Object data = getData(ProjectFileType.STEP_LIST);
        List<ItemData> stepIdList = (List<ItemData>) throwExceptionOnError(data);

        StepData stepData;
        ConverterConfigFile converterConfigFile;
        for (ItemData itemData : stepIdList) {
            data = getData(ProjectFileType.STEP, itemData.getId(), itemData.getId());
            stepData = (StepData) throwExceptionOnError(data);
            converterConfigFile = getConverterConfigFile(stepData, projectUser, fileList, databaseDataList, sftpDataList, localDataList, usedDatabaseIdList, usedSFTPIdList);
            converterMap.put(converterConfigFile.getName().toUpperCase(), converterConfigFile);
        }

        return converterMap;
    }

    private ConverterConfigFile getConverterConfigFile(StepData stepData, ProjectUser projectUser, List<PackageFileData> fileList, List<DatabaseData> databaseDataList, List<SFTPData> sftpDataList, List<LocalData> localDataList, List<Integer> usedDatabaseIdList, List<Integer> usedSFTPIdList) throws IOException, InstantiationException, ClassNotFoundException {
        String fileExt = Defaults.CONFIG_FILE_EXT.getStringValue();
        String fileName = new DConversID(stepData.getName()) + fileExt;
        String loadName = generatedPath + fileName;
        String saveName = FileNameExtension.forName(fileExt.replaceAll("[.]", "")).getBuildPath() + fileName;
        createEmptyFile(loadName);

        ConverterConfigFile converterConfigFile = new ConverterConfigFile(dconvers, loadName, saveName);
        converterConfigFile.setIndex(stepData.getIndex());

        /*all data-tables*/
        Object data = getData(ProjectFileType.DATA_TABLE_LIST, 0, stepData.getId());
        List<Integer> dataTableIdList = (List<Integer>) throwExceptionOnError(data);
        HashMap<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        for (Integer dataTableId : dataTableIdList) {
            data = getData(ProjectFileType.DATA_TABLE, dataTableId, stepData.getId(), dataTableId);
            DataTableData dataTableData = (DataTableData) throwExceptionOnError(data);
            SourceConfig sourceConfig = getSourceConfig(dataTableData, converterConfigFile, projectUser, stepData.getId(), fileList, databaseDataList, sftpDataList, localDataList, usedDatabaseIdList, usedSFTPIdList);
            sourceConfigMap.put(sourceConfig.getName().toUpperCase(), sourceConfig);
        }

        /*all transform-tables*/
        data = getData(ProjectFileType.TRANSFORM_TABLE_LIST, 0, stepData.getId());
        List<Integer> transformTableIdList = (List<Integer>) throwExceptionOnError(data);
        HashMap<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();
        for (Integer transformTableId : transformTableIdList) {
            data = getData(ProjectFileType.TRANSFORM_TABLE, transformTableId, stepData.getId(), 0, transformTableId);
            TransformTableData transformTableData = (TransformTableData) throwExceptionOnError(data);
            TargetConfig targetConfig = getTargetConfig(transformTableData, converterConfigFile, projectUser, stepData.getId());
            targetConfigMap.put(targetConfig.getName().toUpperCase(), targetConfig);
        }

        return converterConfigFile;
    }

    @SuppressWarnings("unchecked")
    private SourceConfig getSourceConfig(DataTableData dataTableData, ConverterConfigFile converterConfigFile, ProjectUser projectUser, int stepId, List<PackageFileData> fileList, List<DatabaseData> databaseDataList, List<SFTPData> sftpDataList, List<LocalData> localDataList, List<Integer> usedDatabaseIdList, List<Integer> usedSFTPIdList) throws IOException, InstantiationException, ClassNotFoundException {
        SourceConfig sourceConfig = new SourceConfig(dconvers, new DConversID(dataTableData.getName()).toString(), converterConfigFile.getProperties());

        sourceConfig.setIndex(dataTableData.getIndex());
        sourceConfig.setId(dataTableData.getIdColName());
        sourceConfig.setTarget(dataTableData.getStartPlug().isPlugged());

        Object data = getData(ProjectFileType.DATA_FILE, dataTableData.getDataFile(), stepId);
        DataFileData dataFileData = (DataFileData) throwExceptionOnError(data);

        DataSourceType dataSourceType = DataSourceType.parse(dataFileData.getDataSourceType());
        if (dataSourceType == null) throw new IOException("Invalid DataSourceType: " + dataFileData.getDataSourceType());

        /*
         * find DataSource and Query.
         * case 1: DataSourceType == Local,Ftp,Http (FileType != SQL) (FileType == CSV, TXT, MD, XSL, JSON, XML, Propeties)
         * case 2: DataSourceType == Database (FileType == SQL)
         * TODO: case 3: Future Feature: DataSourceType == KafkaConsumer (FileType == JSON, XML, JavaSerial)
         * TODO: case 4: Future Feature: DataSourceType == WebServiceRequest[server-less] (FileType == Http)
         */
        String[] fileTypes;
        switch (dataSourceType) {
            case SFTP:
                // datasource from DataFile.type
                fileTypes = dataFileData.getType().split("[_]");
                sourceConfig.setDataSource(fileTypes[1]);

                // query=$[FTP:sftpServerName/path/from/SFTP/NAME.md]
                int sftpId = dataFileData.getDataSourceId();
                SFTPData sftpData = findSftpData(sftpId, sftpDataList);
                String pathFromSFTP = sftpData.getRootPath();
                if (!pathFromSFTP.endsWith("/")) pathFromSFTP += "/";
                String nameFromSFTP = new DConversID(sftpData.getName()) + "/";
                sourceConfig.setQuery("$[FTP:" + nameFromSFTP + pathFromSFTP + dataFileData.getName() + "]");

                if (!usedSFTPIdList.contains(sftpId)) {
                    usedSFTPIdList.add(sftpId);
                    log.debug("usedSFTPIdList.add( {} )", sftpId);
                }
                break;

            case LOCAL:
                // datasource=Markdown // datasource=CSV // datasource=FixedLength // datasource=Lines
                // datasource from DataFile.type
                fileTypes = dataFileData.getType().split("[_]");
                sourceConfig.setDataSource(fileTypes[1]);

                // query=IFRS9/sql/shared/TFSHEADER.md
                // query=path/from/LOCAL/NAME.md
                LocalData localData = findLocalData(dataFileData.getDataSourceId(), localDataList);
                String pathFromLocal = localData.getRootPath();
                if (!pathFromLocal.endsWith("/")) pathFromLocal += "/";
                sourceConfig.setQuery(pathFromLocal + dataFileData.getName());
                break;

            case FIXED:
                // datasource=Markdown // datasource=CSV // datasource=FixedLength // datasource=Lines
                // datasource from DataFile.type
                fileTypes = dataFileData.getType().split("[_]");
                sourceConfig.setDataSource(fileTypes[1]);

                // query=IFRS9/sql/shared/TFSHEADER.md
                // query=build/path/NAME.md
                PackageFileData localPackageFileData = findUploadedFileData(dataFileData, fileList);
                sourceConfig.setQuery(localPackageFileData.getBuildPath() + localPackageFileData.getName());
                break;

            case DATABASE:
                // datasource from DataBaseId
                int databaseId = dataFileData.getDataSourceId();
                DatabaseData databaseData = findDatabaseData(databaseId, databaseDataList);
                String nameFromDabase = new DConversID(databaseData.getName()).toString();
                sourceConfig.setDataSource(nameFromDabase);

                if (!usedDatabaseIdList.contains(databaseId)) usedDatabaseIdList.add(databaseId);

                // query from content of DataFile.name
                /* query=$[TXT:IFRS9/sql/shared/TFSHEADER.sql] */
                PackageFileData sqlPackageFileData = findUploadedFileData(dataFileData, fileList);
                sourceConfig.setQuery("$[TXT:" + sqlPackageFileData.getBuildPath() + sqlPackageFileData.getName() + "]");
                break;

            case SYSTEM:
                // datasource=SYSTEM
                // query=environment // query=os_variable // query=variable // query=memory
                sourceConfig.setDataSource(dataSourceType.name());
                sourceConfig.setQuery(dataFileData.getName());
                break;

            case DIR:
                // datasource=DIR
                // query=path/to/directory
                sourceConfig.setDataSource(dataSourceType.name());
                String dir = (String) dataFileData.getPropertyMap().get(PropertyVar.dir.name());
                dir = dir.replaceAll("[\\\\]", "/");
                sourceConfig.setQuery(dir.endsWith("/") ? dir.substring(0, dir.length() - 1) : dir);
                break;

        }

        /*Notice: IMPORTANT: DataTable ColumnList will load by Query automatically at runtime, once concern before this point if DataSource is not LOCAL need to confirm structure from Uploaded File is corrected*/

        /*all outputs of datatable*/
        data = getData(ProjectFileType.DATA_OUTPUT_LIST, 0, stepId, dataTableData.getId());
        List<Integer> outputIdList = (List<Integer>) throwExceptionOnError(data);

        OutputConfig outputConfig = sourceConfig.getOutputConfig();
        OutputFileData outputFileData;
        for (Integer outputId : outputIdList) {
            data = getData(ProjectFileType.DATA_OUTPUT, outputId, stepId, dataTableData.getId());
            outputFileData = (OutputFileData) throwExceptionOnError(data);
            setOutputConfig(outputConfig, outputFileData);
        }

        return sourceConfig;
    }

    private DatabaseData findDatabaseData(int dataSourceId, List<DatabaseData> databaseDataList) throws IOException {
        for (DatabaseData databaseData : databaseDataList) {
            if (databaseData.getId() == dataSourceId) {
                return databaseData;
            }
        }
        throw new IOException("Databbase data not found: id=" + dataSourceId + " '");
    }

    private LocalData findLocalData(int dataSourceId, List<LocalData> localDataList) throws IOException {
        for (LocalData localData : localDataList) {
            if (localData.getId() == dataSourceId) {
                return localData;
            }
        }
        throw new IOException("LOCAL data not found: id=" + dataSourceId + " '");
    }

    private SFTPData findSftpData(int dataSourceId, List<SFTPData> sftpDataList) throws IOException {
        for (SFTPData sftpData : sftpDataList) {
            if (sftpData.getId() == dataSourceId) {
                return sftpData;
            }
        }
        throw new IOException("SFTP data not found: id=" + dataSourceId + " '");
    }

    private PackageFileData findUploadedFileData(DataFileData dataFileData, List<PackageFileData> fileList) throws IOException {
        for (PackageFileData fileData : fileList) {
            if (FileType.UPLOADED == fileData.getType() && fileData.getFileId() == dataFileData.getUploadedId()) {
                return fileData;
            }
        }
        throw new IOException("Uploaded file not found: id=" + dataFileData.getUploadedId() + " '" + dataFileData.getName() + "'");
    }

    private BinaryFileItemData getBinaryFileItemData(int uploadedId, List<BinaryFileItemData> binaryFileItemDataList) {
        for (BinaryFileItemData binaryFileItemData : binaryFileItemDataList) {
            if (binaryFileItemData.getId() == uploadedId) return binaryFileItemData;
        }
        return null;
    }

    private void setOutputConfig(OutputConfig outputConfig, OutputFileData outputFileData) {
        DataFileType outputFileType = DataFileType.parse(outputFileData.getType());
        if (outputFileType == null) return;

        switch (outputFileType) {
            case OUT_SQL:
                setOutputSQL(outputConfig, outputFileData);
                break;
            case OUT_MD:
                setOutputMD(outputConfig, outputFileData);
                break;
            case OUT_CSV:
                setOutputCSV(outputConfig, outputFileData);
                break;
            case OUT_TXT:
                setOutputTXT(outputConfig, outputFileData);
                break;
        }
    }

    private void setOutputSQL(OutputConfig outputConfig, OutputFileData outputFileData) {
        Map<String, Object> propertyMap = outputFileData.getPropertyMap();
        /*TODO: future feature: set output for SQL*/
    }

    private void setOutputMD(OutputConfig outputConfig, OutputFileData outputFileData) {
        Map<String, Object> propertyMap = outputFileData.getPropertyMap();
        outputConfig.setMarkdown(true);
        outputConfig.setMarkdownOutput(normalizeOutputFilePath(/*TODO: get path from datasource(FTP/LOCAL)*/"") + normalizeOutputFileName(outputFileData.getName()));
        outputConfig.setMarkdownOutputAppend((Boolean) propertyMap.get("append"));
        outputConfig.setMarkdownOutputCharset((String) propertyMap.get("charset"));
        outputConfig.setMarkdownOutputEOL((String) propertyMap.get("eol"));
        outputConfig.setMarkdownOutputEOF((String) propertyMap.get("eof"));
        outputConfig.setMarkdownComment((Boolean) propertyMap.get("showComment"));
        outputConfig.setMarkdownCommentDataSource((Boolean) propertyMap.get("showDataSource"));
        outputConfig.setMarkdownCommentQuery((Boolean) propertyMap.get("showQuery"));
        outputConfig.setMarkdownTitle((Boolean) propertyMap.get("showTableTitle"));
        outputConfig.setMarkdownRowNumber((Boolean) propertyMap.get("showRowNumber"));
        outputConfig.setMarkdownMermaid((Boolean) propertyMap.get("showFlowChart"));
        outputConfig.setMarkdownMermaidFull((Boolean) propertyMap.get("showLongFlowChart"));
    }

    private String normalizeOutputFileName(String name) {
        if (name == null) return "output";
        return name.replaceAll("\\p{Punct}", "");
    }

    private String normalizeOutputFilePath(String path) {
        if (path == null) return "";
        path = path.replaceAll("//|///|////", "/");
        if (path.startsWith("/")) path = path.substring(1);
        if (!path.endsWith("/")) path += "/";
        return path;
    }

    private void setOutputCSV(OutputConfig outputConfig, OutputFileData outputFileData) {
        Map<String, Object> propertyMap = outputFileData.getPropertyMap();
        /*TODO: future feature: set output for CSV*/
    }

    private void setOutputTXT(OutputConfig outputConfig, OutputFileData outputFileData) {
        Map<String, Object> propertyMap = outputFileData.getPropertyMap();
        /*TODO: future feature: set output for TXT*/
    }

    @SuppressWarnings("unchecked")
    private TargetConfig getTargetConfig(TransformTableData transformTableData, ConverterConfigFile converterConfigFile, ProjectUser projectUser, int stepId) throws IOException, InstantiationException, ClassNotFoundException {
        TargetConfig targetConfig = new TargetConfig(dconvers, new DConversID(transformTableData.getName()).toString(), converterConfigFile.getProperties());

        /*TODO: future feature: merge 2 or more sourceTables to a targetTable*/
        SourceType sourceType = SourceType.valueOf(transformTableData.getSourceType());
        String sourceKey = (SourceType.DATA_TABLE == sourceType ? IDPrefix.DATA_TABLE.getPrefix() : IDPrefix.TRANSFORM_TABLE.getPrefix()) + transformTableData.getSourceId();
        targetConfig.setSource(sourceKey);
        targetConfig.getSourceList().add(targetConfig.getSource());

        targetConfig.setIndex(transformTableData.getIndex());
        targetConfig.setId(transformTableData.getIdColName());

        /*all transform-columns*/
        Object data = getData(ProjectFileType.TRANSFORM_COLUMN_LIST, 0, stepId, 0, transformTableData.getId());
        List<Integer> columnIdList = (List<Integer>) throwExceptionOnError(data);
        List<Pair<String, String>> columnList = targetConfig.getColumnList();
        TransformColumnData transformColumnData;
        for (Integer columnId : columnIdList) {
            data = getData(ProjectFileType.TRANSFORM_COLUMN, columnId, stepId, 0, transformTableData.getId());
            transformColumnData = (TransformColumnData) throwExceptionOnError(data);

            /* ColumnFx has 3 cases for arguments:
             * 1. direct transfer >> need to get columnName by columnId from sourceTable
             *      ( useDynamicValue == false )
             * 2. dynamic value expression >> ready to use value
             *      ( useDynamicValue == true && useFunction == false )
             * 3. use DynamicValueType == CAL function >> get specific-function arguments
             * 3. use DynamicValueType != CAL function >> get specific-function arguments
             *      ( useFunction == true )
             * */
            if (!transformColumnData.isUseDynamic()) {
                /*case 1.*/
                Object columnData;
                if (SourceType.DATA_TABLE == sourceType) {
                    columnData = getData(ProjectFileType.DATA_COLUMN, transformColumnData.getSourceColumnId(), stepId, transformTableData.getSourceId());
                } else {
                    columnData = getData(ProjectFileType.TRANSFORM_COLUMN, transformColumnData.getSourceColumnId(), stepId, 0, transformTableData.getSourceId());
                }
                DataColumnData dataColumnData = (DataColumnData) throwExceptionOnError(columnData);
                columnList.add(new Pair<>(transformColumnData.getName(), dataColumnData.getName()));
            } else if (!transformColumnData.isUseFunction()) {
                /*case 2.*/
                columnList.add(new Pair<>(transformColumnData.getName(), transformColumnData.getDynamicExpression()));
            } else {
                /*case 3.*/
                StringBuilder dynamicValueBuilder = new StringBuilder();

                String function = transformColumnData.getFunction();
                boolean isCalc = DynamicValueType.parse(function) == null;
                dynamicValueBuilder.append(isCalc ? "CALC" : function);
                dynamicValueBuilder.append(":");

                if (isCalc) {
                    dynamicValueBuilder.append(function);
                    dynamicValueBuilder.append("(");
                }

                /*create ordered-arguments*/
                dynamicValueBuilder.append(toArguments(transformColumnData.getPropertyMap(), transformColumnData.getPropertyOrder()));

                if (isCalc) {
                    dynamicValueBuilder.append(")");
                }

                columnList.add(new Pair<>(transformColumnData.getName(), dynamicValueBuilder.toString()));
            }
        }

        /*all transformations*/
        data = getData(ProjectFileType.TRANSFORMATION_LIST, transformTableData.getId(), stepId, 0, transformTableData.getId());
        List<Integer> tableFxDataIdList = (List<Integer>) throwExceptionOnError(data);
        TransformConfig transformConfig = targetConfig.getTransformConfig();
        TableFxData tableFxData;
        for (Integer tableFxDataId : tableFxDataIdList) {
            data = getData(ProjectFileType.TRANSFORMATION, tableFxDataId, stepId, 0, transformTableData.getId());
            tableFxData = (TableFxData) throwExceptionOnError(data);
            addTransformations(tableFxData, transformConfig);
        }

        /*all outputs of transformtable*/
        data = getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, 0, stepId, transformTableData.getId());
        List<Integer> outputIdList = (List<Integer>) throwExceptionOnError(data);
        OutputConfig outputConfig = targetConfig.getOutputConfig();
        OutputFileData outputFileData;
        for (Integer outputId : outputIdList) {
            data = getData(ProjectFileType.TRANSFORM_OUTPUT, outputId, stepId, transformTableData.getId());
            outputFileData = (OutputFileData) throwExceptionOnError(data);
            setOutputConfig(outputConfig, outputFileData);
        }

        /*-- may be in the future features
        outputConfig = targetConfig.getMappingOutputConfig();
        */

        /*-- may be in the future features
        outputConfig = targetConfig.getTransferOutputConfig();
        */

        return targetConfig;
    }

    private void addTransformations(TableFxData tableFxData, TransformConfig transformConfig) throws IOException {
        /* Have 2 cases for arguments:
         * 1. dynamic value expression >> ready to use value
         *      ( useFunction == false )
         * 2. use function >> get specific-function arguments
         *      ( useFunction == true )
         * */
        if (!tableFxData.isUseFunction()) {
            /*case 1: dynamic value expression*/
            transformConfig.addTransforms((String) tableFxData.getPropertyMap().get("dynamicValue"));
            return;
        }

        /*case 2: specific function arguments*/
        TransformTypes transformTypes = TransformTypes.parse(tableFxData.getFunction());
        if (transformTypes == null) {
            throw new IOException("Not support transformation '" + tableFxData.getFunction() + "' on table '" + tableFxData.getName() + "'");
        }

        /*create ordered-arguments*/
        HashMap<String, String> argumentMap = new HashMap<>();
        argumentMap.put("arguments", toArguments(tableFxData.getPropertyMap(), tableFxData.getPropertyOrder()));

        transformConfig.getTransformList().add(new Pair<>(transformTypes, argumentMap));
    }

    private String toArguments(Map<String, Object> propertyMap, String propertyOrder) {
        StringBuilder arguments = new StringBuilder();
        String[] keys = propertyOrder.split("[,]");
        for (String key : keys) {
            arguments.append(",").append(propertyMap.get(key).toString());
        }
        return arguments.substring(1);
    }

    @Override
    public String toString() {
        return "BuildPackageCommand{" +
                "offset:" + offset +
                ", key:'" + key + '\'' +
                (attributes == null ? "" : ", attributes:" + attributes) +
                '}';
    }
}
