package com.tflow.tbcmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.*;
import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.clevel.dconvers.ngin.Crypto;
import com.clevel.dconvers.transform.TransformTypes;
import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.util.DateTimeUtil;
import com.tflow.wcmd.KafkaCommand;
import javafx.util.Pair;
import org.mapstruct.ap.shaded.freemarker.template.utility.StringUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BuildPackageCommand extends KafkaCommand {

    private Logger log = LoggerFactory.getLogger(BuildPackageCommand.class);

    private ProjectDataManager dataManager;
    private PackageMapper mapper;

    private String generatedPath;
    private DConvers dconvers;

    public BuildPackageCommand(String key, Object value, EnvironmentConfigs environmentConfigs, ProjectDataManager dataManager) {
        super(key, value, environmentConfigs);
        this.dataManager = dataManager;
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException {
        mapper = Mappers.getMapper(PackageMapper.class);

        /*Notice: key is command, now have only 1 'build' command*/
        /*Notice: first version assume ProjectType always be BATCH*/
        KafkaRecordAttributes attributes = (KafkaRecordAttributes) value;
        ProjectUser projectUser = mapper.map(attributes);

        /*Object data = dataManager.getData(ProjectFileType.PROJECT, projectUser, projectUser.getId());
        ProjectData projectData = (ProjectData) throwExceptionOnError(data);*/

        Object data = dataManager.getData(ProjectFileType.PACKAGE_LIST, projectUser);
        //noinspection unchecked (suppress warning about unchecked)
        List<PackageItemData> packageIdList = (List<PackageItemData>) throwExceptionOnError(data);
        int packageId = packageIdList.size();

        /*Notice: packageData contains percent complete for ui, update them 4-5 times max*/
        PackageData packageData = new PackageData();
        packageData.setPackageId(packageId);
        packageData.setProjectId(attributes.getProjectId());
        packageData.setBuildDate(DateTimeUtil.now());
        packageData.setName("building...");

        packageIdList.add(mapper.map(packageData));
        dataManager.addData(ProjectFileType.PACKAGE_LIST, packageIdList, projectUser);
        updatePercentComplete(packageData, projectUser, 0, estimateBuiltDate());

        List<PackageFileData> fileList = new ArrayList<>();
        addUploadedFiles(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 20, estimateBuiltDate());

        addGeneratedFiles(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 50, estimateBuiltDate());

        addVersionedFiles(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 75, estimateBuiltDate());

        addConfigVersionFile(fileList, packageData, projectUser);
        packageData.setFileList(fileList);
        updatePercentComplete(packageData, projectUser, 100, DateTimeUtil.now());
    }

    private void addConfigVersionFile(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) {
        /*TODO: future feature: add Configuration Version File to package-file-list*/

    }

    private void updatePercentComplete(PackageData packageData, ProjectUser projectUser, int percent, Date builtDate) {
        packageData.setComplete(percent);
        packageData.setBuiltDate(builtDate);
        dataManager.addData(ProjectFileType.PACKAGE, packageData, projectUser);
    }

    @SuppressWarnings("unchecked")
    private void addVersionedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) throws IOException {
        /*TODO: future feature: need to filter by ProjectType on next ProjectType*/
        Object data = dataManager.getData(ProjectFileType.VERSIONED_LIST, projectUser);
        List<BinaryFileItemData> binaryFileItemDataList = (List<BinaryFileItemData>) throwExceptionOnError(data);
        for (BinaryFileItemData binaryFileItemData : binaryFileItemDataList) {
            PackageFileData packageFileData = mapper.map(binaryFileItemData);
            packageFileData.setId(newPackageFileId(packageData));
            packageFileData.setType(FileType.VERSIONED);
            packageFileData.setBuildDate(packageData.getBuildDate());
            fileList.add(packageFileData);
        }
    }

    @SuppressWarnings("unchecked")
    private void addUploadedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) throws IOException {
        Object data = dataManager.getData(ProjectFileType.UPLOADED_LIST, projectUser);
        List<BinaryFileItemData> binaryFileItemDataList = (List<BinaryFileItemData>) throwExceptionOnError(data);
        for (BinaryFileItemData binaryFileItemData : binaryFileItemDataList) {
            PackageFileData packageFileData = mapper.map(binaryFileItemData);
            packageFileData.setId(newPackageFileId(packageData));
            packageFileData.setType(FileType.UPLOADED);
            packageFileData.setBuildDate(packageData.getBuildDate());
            packageFileData.setBuildPath(FileNameExtension.forName(binaryFileItemData.getName()).getBuildPath());
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

    private void addGeneratedFiles(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) throws IOException {
        int packageFileId = newPackageFileId(packageData);
        generatedPath = environmentConfigs.getProjectRootPath() + projectUser.getId() + "/" + ProjectFileType.GENERATED.name().toLowerCase() + "/";
        String name = packageFileId + Defaults.CONFIG_FILE_EXT.getStringValue();
        createEmptyFile(generatedPath + name);

        BinaryFileData conversionFileData = new BinaryFileData();
        conversionFileData.setId(newPackageFileId(packageData));
        conversionFileData.setName(name);
        conversionFileData.setExt(FileNameExtension.forName(name));

        dconvers = new DConvers(new String[]{
                "--library-mode=manual"
                /*, "--source-type=" + ConfigFileTypes.PROPERTIES.name()*/
                , "--source=" + generatedPath + name
                /*,"--save-default-value"*/
        });

        DataConversionConfigFile dataConversionConfigFile = dconvers.dataConversionConfigFile;
        initDataConversionConfigFile(dataConversionConfigFile, projectUser, fileList);

        try {
            log.info("dataConversionConfigFile.saveProperties...");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            dataConversionConfigFile.saveProperties(byteArrayOutputStream);

            /*create Generated Conversion File*/
            conversionFileData.setContent(byteArrayOutputStream.toByteArray());
            dataManager.addData(ProjectFileType.GENERATED, conversionFileData, projectUser, conversionFileData.getId());
            fileList.add(mapper.map(conversionFileData));

            BinaryFileData converterFileData;
            for (ConverterConfigFile converterConfigFile : dataConversionConfigFile.getConverterConfigMap().values()) {
                name = extractFileName(converterConfigFile.getName());

                byteArrayOutputStream = new ByteArrayOutputStream();
                converterConfigFile.saveProperties(byteArrayOutputStream);

                /*create Generated Converter File*/
                converterFileData = new BinaryFileData();
                converterFileData.setId(newPackageFileId(packageData));
                converterFileData.setName(name);
                converterFileData.setExt(FileNameExtension.forName(name));
                converterFileData.setContent(byteArrayOutputStream.toByteArray());
                dataManager.addData(ProjectFileType.GENERATED, converterFileData, projectUser, converterFileData.getId());

                fileList.add(mapper.map(converterFileData));
            }

            log.info("generate dconvers-config-files success.\n");
        } catch (Exception ex) {
            log.error("generate dconvers-config-files error: ", ex);
            throw new IOException("generate file failed: ", ex);
        }
    }

    private String extractFileName(String name) {
        String[] names = name.split("[/]");
        return names[names.length - 1];
    }

    private void createEmptyFile(String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException ex) {
            log.error("createEmptyFile(" + fileName + ") failed, ", ex);
        }
    }

    private void initDataConversionConfigFile(DataConversionConfigFile dataConversionConfigFile, ProjectUser projectUser, List<PackageFileData> fileList) throws IOException {
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

        dataConversionConfigFile.setDataSourceConfigMap(createDataSourceConfigMap(projectUser));
        dataConversionConfigFile.setSftpConfigMap(createSftpConfigMap(projectUser));
        dataConversionConfigFile.setSmtpConfigMap(createSmtpConfigMap());

        dataConversionConfigFile.setConverterConfigMap(getConverterConfigMap(projectUser, fileList));
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, DataSourceConfig> createDataSourceConfigMap(ProjectUser projectUser) throws IOException {
        HashMap<String, DataSourceConfig> dataSourceConfigHashMap = new HashMap<>();

        Object data = dataManager.getData(ProjectFileType.DB_LIST, projectUser);
        List<Integer> dbIdList = (List<Integer>) throwExceptionOnError(data);

        DatabaseData databaseData;
        DataSourceConfig dataSourceConfig;
        for (Integer databaseId : dbIdList) {
            data = dataManager.getData(ProjectFileType.DB, projectUser, databaseId);
            databaseData = (DatabaseData) throwExceptionOnError(data);
            dataSourceConfig = getDataSourceConfig(databaseData);
            dataSourceConfigHashMap.put(dataSourceConfig.getName().toUpperCase(), dataSourceConfig);
        }

        return dataSourceConfigHashMap;
    }

    private DataSourceConfig getDataSourceConfig(DatabaseData databaseData) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(dconvers, IDPrefix.DB.getPrefix() + databaseData.getId());

        dataSourceConfig.setUrl(/*"jdbc:oracle:thin:@172.20.8.67:1521:FCUAT2"*/ databaseData.getUrl());
        dataSourceConfig.setDriver(/*"oracle.jdbc.driver.OracleDriver"*/databaseData.getDriver());
        dataSourceConfig.setSchema(/*"account"*/ "");
        dataSourceConfig.setUser(databaseData.isUserEncrypted() ? Crypto.decrypt(databaseData.getUser()) : databaseData.getUser());
        dataSourceConfig.setPassword(databaseData.isPasswordEncrypted() ? Crypto.decrypt(databaseData.getPassword()) : databaseData.getPassword());
        dataSourceConfig.setRetry(databaseData.getRetry());

        /*-- for EmailDataSource
        dataSourceConfig.setSsl(false);
        dataSourceConfig.setHost("localhost:5210");*/

        /*dataSourceConfig.setValueQuotes("'");
        dataSourceConfig.setNameQuotes("'");*/

        dataSourceConfig.setUserEncrypted(databaseData.isUserEncrypted());
        dataSourceConfig.setPasswordEncrypted(databaseData.isPasswordEncrypted());

        /*List<Pair<String, String>> propList = new ArrayList<>();
        propList.add(new Pair<>("autoCommit", "false"));
        dataSourceConfig.setPropList(propList);*/

        /*dataSourceConfig.setPre("set system=1;");
        dataSourceConfig.setPost("commit;set system=0;");*/

        return dataSourceConfig;
    }

    private String wordsAsId(String name) {
        return StringUtil.capitalize(name.replaceAll("\\p{Punct}|\\s", ""));
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, HostConfig> createSftpConfigMap(ProjectUser projectUser) throws IOException {
        HashMap<String, HostConfig> sftpConfigMap = new HashMap<>();

        Object data = dataManager.getData(ProjectFileType.SFTP_LIST, projectUser);
        List<Integer> sftpIdList = (List<Integer>) throwExceptionOnError(data);

        SFTPData sftpData;
        HostConfig hostConfig;
        for (Integer sftpId : sftpIdList) {
            data = dataManager.getData(ProjectFileType.SFTP, projectUser, sftpId);
            sftpData = (SFTPData) throwExceptionOnError(data);
            hostConfig = getHostConfig(sftpData);
            sftpConfigMap.put(hostConfig.getName().toUpperCase(), hostConfig);
        }

        return sftpConfigMap;
    }

    private HostConfig getHostConfig(SFTPData sftpData) {
        HostConfig hostConfig = new HostConfig(dconvers, IDPrefix.SFTP.getPrefix() + sftpData.getId(), Property.SFTP);

        hostConfig.setHost(sftpData.getHost());
        hostConfig.setPort(sftpData.getPort());
        hostConfig.setUser(sftpData.isUserEncrypted() ? Crypto.decrypt(sftpData.getUser()) : sftpData.getUser());
        hostConfig.setPassword(sftpData.isPasswordEncrypted() ? Crypto.decrypt(sftpData.getPassword()) : sftpData.getPassword());
        hostConfig.setRetry(sftpData.getRetry());
        hostConfig.setTmp(sftpData.getTmp());

        return hostConfig;
    }

    private HashMap<String, HostConfig> createSmtpConfigMap() {
        /*TODO: Future Feature: generate smtpConfigMap*/
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, ConverterConfigFile> getConverterConfigMap(ProjectUser projectUser, List<PackageFileData> fileList) throws IOException {
        HashMap<String, ConverterConfigFile> converterMap = new HashMap<>();

        Object data = dataManager.getData(ProjectFileType.STEP_LIST, projectUser);
        List<StepItemData> stepIdList = (List<StepItemData>) throwExceptionOnError(data);

        StepData stepData;
        ConverterConfigFile converterConfigFile;
        for (StepItemData stepItemData : stepIdList) {
            data = dataManager.getData(ProjectFileType.STEP, projectUser, stepItemData.getId(), stepItemData.getId());
            stepData = (StepData) throwExceptionOnError(data);
            converterConfigFile = getConverterConfigFile(stepData, projectUser, fileList);
            converterMap.put(converterConfigFile.getName().toUpperCase(), converterConfigFile);
        }

        return converterMap;
    }

    private ConverterConfigFile getConverterConfigFile(StepData stepData, ProjectUser projectUser, List<PackageFileData> fileList) throws IOException {
        String fileName = generatedPath + IDPrefix.STEP.getPrefix() + stepData.getId() + Defaults.CONFIG_FILE_EXT.getStringValue();
        createEmptyFile(fileName);

        ConverterConfigFile converterConfigFile = new ConverterConfigFile(dconvers, fileName);
        converterConfigFile.setIndex(stepData.getIndex());

        /*all data-tables*/
        Object data = dataManager.getData(ProjectFileType.DATA_TABLE_LIST, projectUser, 0, stepData.getId());
        List<Integer> dataTableIdList = (List<Integer>) throwExceptionOnError(data);
        HashMap<String, SourceConfig> sourceConfigMap = converterConfigFile.getSourceConfigMap();
        for (Integer dataTableId : dataTableIdList) {
            data = dataManager.getData(ProjectFileType.DATA_TABLE, projectUser, dataTableId, stepData.getId(), dataTableId);
            DataTableData dataTableData = (DataTableData) throwExceptionOnError(data);
            SourceConfig sourceConfig = getSourceConfig(dataTableData, converterConfigFile, projectUser, stepData.getId(), fileList);
            sourceConfigMap.put(sourceConfig.getName().toUpperCase(), sourceConfig);
        }

        /*all transform-tables*/
        data = dataManager.getData(ProjectFileType.TRANSFORM_TABLE_LIST, projectUser, 0, stepData.getId());
        List<Integer> transformTableIdList = (List<Integer>) throwExceptionOnError(data);
        HashMap<String, TargetConfig> targetConfigMap = converterConfigFile.getTargetConfigMap();
        for (Integer transformTableId : transformTableIdList) {
            data = dataManager.getData(ProjectFileType.TRANSFORM_TABLE, projectUser, transformTableId, stepData.getId(), 0, transformTableId);
            TransformTableData transformTableData = (TransformTableData) throwExceptionOnError(data);
            TargetConfig targetConfig = getTargetConfig(transformTableData, converterConfigFile, projectUser, stepData.getId());
            targetConfigMap.put(targetConfig.getName().toUpperCase(), targetConfig);
        }

        return converterConfigFile;
    }

    @SuppressWarnings("unchecked")
    private SourceConfig getSourceConfig(DataTableData dataTableData, ConverterConfigFile converterConfigFile, ProjectUser projectUser, int stepId, List<PackageFileData> fileList) throws IOException {
        SourceConfig sourceConfig = new SourceConfig(dconvers, IDPrefix.DATA_TABLE.getPrefix() + dataTableData.getId(), converterConfigFile);

        sourceConfig.setIndex(dataTableData.getIndex());
        sourceConfig.setId(dataTableData.getIdColName());
        sourceConfig.setTarget(dataTableData.getStartPlug().getLineList().size() > 0);

        Object data = dataManager.getData(ProjectFileType.DATA_FILE, projectUser, dataTableData.getDataFile(), stepId);
        DataFileData dataFileData = (DataFileData) throwExceptionOnError(data);
        Integer lineToDataSourceId = dataFileData.getEndPlug().getLineList().get(0);

        /* dataSourceSelector stand at startPlug of a line between dataSource and dataFile */
        data = dataManager.getData(ProjectFileType.LINE, projectUser, lineToDataSourceId, stepId);
        LineData lineData = (LineData) throwExceptionOnError(data);
        String dataSourceSelectorId = lineData.getStartSelectableId().substring(IDPrefix.DATA_SOURCE_SELECTOR.getPrefix().length());

        data = dataManager.getData(ProjectFileType.DATA_SOURCE_SELECTOR, projectUser, Integer.parseInt(dataSourceSelectorId), stepId);
        DataSourceSelectorData dataSourceSelectorData = (DataSourceSelectorData) throwExceptionOnError(data);
        DataSourceType dataSourceType = DataSourceType.parse(dataSourceSelectorData.getType());
        if (dataSourceType == null) throw new IOException("Invalid DataSourceType: " + dataSourceSelectorData.getType());

        /*
         * find DataSource and Query.
         * case 1: DataSourceType == Local,Ftp,Http (FileType != SQL) (FileType == CSV, TXT, MD, XSL, JSON, XML, Propeties)
         * case 2: DataSourceType == Database (FileType == SQL)
         * TODO: case 3: Future Feature: DataSourceType == KafkaConsumer (FileType == JSON, XML, JavaSerial)
         * TODO: case 4: Future Feature: DataSourceType == WebServiceRequest[server-less] (FileType == Http)
         */
        switch (dataSourceType) {
            case SFTP:
                // datasource from DataFile.type
                String[] types = dataFileData.getType().split("[_]");
                sourceConfig.setDataSource(types[1]);

                // query=$[FTP:sftpserver/SFTP-Staging-Path/ALITLNDP_STEP2.md]
                PackageFileData sftpPackageFileData = findUploadedFileData(dataFileData, fileList);
                sourceConfig.setQuery("$[FTP:" + IDPrefix.SFTP.getPrefix() + dataSourceSelectorData.getDataSourceId() + "/" + sftpPackageFileData.getBuildPath() + sftpPackageFileData.getName() + "]");
                break;

            case LOCAL:
                // datasource=Markdown // datasource=CSV // datasource=FixedLength // datasource=Lines
                // datasource from DataFile.type
                String[] fileTypes = dataFileData.getType().split("[_]");
                sourceConfig.setDataSource(fileTypes[1]);

                // query=/SFTP-Staging-Path/ALITLNDP_STEP2.md
                /* query=IFRS9/sql/shared/TFSHEADER.md */
                PackageFileData localPackageFileData = findUploadedFileData(dataFileData, fileList);
                sourceConfig.setQuery(localPackageFileData.getBuildPath() + localPackageFileData.getName());
                break;

            case SYSTEM:
                // datasource=SYSTEM
                // query=environment // query=os_variable // query=variable // query=memory
                sourceConfig.setDataSource(dataSourceType.name());
                sourceConfig.setQuery(dataFileData.getType());
                break;

            case DATABASE:
                // datasource from DataBaseId
                sourceConfig.setDataSource(IDPrefix.DB.getPrefix() + dataSourceSelectorData.getDataSourceId());

                // query from content of DataFile.name
                /* query=$[TXT:IFRS9/sql/shared/TFSHEADER.sql] */
                PackageFileData sqlPackageFileData = findUploadedFileData(dataFileData, fileList);
                sourceConfig.setQuery("$[TXT:" + sqlPackageFileData.getBuildPath() + sqlPackageFileData.getName() + "]");
                break;
        }

        /*Notice: IMPORTANT: DataTable ColumnList will load by Query automatically, once concern before this point if DataSource is not LOCAL need to confirm structure from Uploaded File is corrected*/

        /*all outputs of datatable*/
        data = dataManager.getData(ProjectFileType.DATA_OUTPUT_LIST, projectUser, 0, stepId, dataTableData.getId());
        List<Integer> outputIdList = (List<Integer>) throwExceptionOnError(data);

        OutputConfig outputConfig = sourceConfig.getOutputConfig();
        OutputFileData outputFileData;
        for (Integer outputId : outputIdList) {
            data = dataManager.getData(ProjectFileType.DATA_OUTPUT, projectUser, outputId, stepId, dataTableData.getId());
            outputFileData = (OutputFileData) throwExceptionOnError(data);
            setOutputConfig(outputConfig, outputFileData);
        }

        return sourceConfig;
    }

    private PackageFileData findUploadedFileData(DataFileData dataFileData, List<PackageFileData> fileList) throws IOException {
        /*Notice: uploaded files need to added before*/
        PackageFileData sqlPackageFileData = null;
        for (PackageFileData fileData : fileList) {
            if (FileType.UPLOADED == fileData.getType() && fileData.getFileId() == dataFileData.getUploadedId()) {
                sqlPackageFileData = fileData;
                break;
            }
        }
        if (sqlPackageFileData == null) {
            throw new IOException("Uploaded file not found: id=" + dataFileData.getUploadedId() + " '" + dataFileData.getName() + "'");
        }
        return sqlPackageFileData;
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
        outputConfig.setMarkdownOutput(normalizeOutputFilePath(outputFileData.getPath()) + normalizeOutputFileName(outputFileData.getName()));
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
    private TargetConfig getTargetConfig(TransformTableData transformTableData, ConverterConfigFile converterConfigFile, ProjectUser projectUser, int stepId) throws IOException {
        TargetConfig targetConfig = new TargetConfig(dconvers, IDPrefix.TRANSFORM_TABLE.getPrefix() + transformTableData.getId(), converterConfigFile);

        /*TODO: future feature: merge 2 or more sourceTables to a targetTable*/
        targetConfig.setSource(transformTableData.getSourceSelectableId());
        targetConfig.getSourceList().add(targetConfig.getSource());

        targetConfig.setIndex(transformTableData.getIndex());
        targetConfig.setId(transformTableData.getIdColName());

        /*all transform-columns*/
        Object data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN_LIST, projectUser, 0, stepId, 0, transformTableData.getId());
        List<Integer> columnIdList = (List<Integer>) throwExceptionOnError(data);
        List<Pair<String, String>> columnList = targetConfig.getColumnList();
        TransformColumnData transformColumnData;
        for (Integer columnId : columnIdList) {
            data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN, projectUser, columnId, stepId, 0, transformTableData.getId());
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
                columnList.add(new Pair<>(transformColumnData.getName(), transformColumnData.getDataColName()));
            } else if (!transformColumnData.isUseFunction()) {
                /*case 2.*/
                columnList.add(new Pair<>(transformColumnData.getName(), transformColumnData.getDataColName()));
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
        data = dataManager.getData(ProjectFileType.TRANSFORMATION_LIST, projectUser, transformTableData.getId(), stepId, 0, transformTableData.getId());
        List<Integer> tableFxDataIdList = (List<Integer>) throwExceptionOnError(data);
        TransformConfig transformConfig = targetConfig.getTransformConfig();
        TableFxData tableFxData;
        for (Integer tableFxDataId : tableFxDataIdList) {
            data = dataManager.getData(ProjectFileType.TRANSFORMATION, projectUser, tableFxDataId, stepId, 0, transformTableData.getId());
            tableFxData = (TableFxData) throwExceptionOnError(data);
            addTransformations(tableFxData, transformConfig);
        }

        /*all outputs of transformtable*/
        data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, projectUser, 0, stepId, transformTableData.getId());
        List<Integer> outputIdList = (List<Integer>) throwExceptionOnError(data);
        OutputConfig outputConfig = targetConfig.getOutputConfig();
        OutputFileData outputFileData;
        for (Integer outputId : outputIdList) {
            data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT, projectUser, outputId, stepId, transformTableData.getId());
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

}
