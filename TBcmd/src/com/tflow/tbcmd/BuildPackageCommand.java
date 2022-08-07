package com.tflow.tbcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.util.DateTimeUtil;
import com.tflow.wcmd.KafkaCommand;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuildPackageCommand extends KafkaCommand {

    Logger log = LoggerFactory.getLogger(BuildPackageCommand.class);

    ProjectDataManager projectDataManager;

    public BuildPackageCommand(String key, Object value, EnvironmentConfigs environmentConfigs, ProjectDataManager projectDataManager) {
        super(key, value, environmentConfigs);
        this.projectDataManager = projectDataManager;
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException {
        PackageMapper mapper = Mappers.getMapper(PackageMapper.class);

        /*Notice: key is command, now have only 1 'build' command*/
        /*Notice: first version assume ProjectType always be BATCH*/
        KafkaRecordAttributes attributes = (KafkaRecordAttributes) value;
        ProjectUser projectUser = mapper.map(attributes);

        Object data = projectDataManager.getData(ProjectFileType.PROJECT, projectUser, projectUser.getId());
        ProjectData projectData = (ProjectData) throwExceptionOnError(data);

        data = projectDataManager.getData(ProjectFileType.PACKAGE_LIST, projectUser);
        //noinspection unchecked (suppress warning about unchecked)
        List<PackageItemData> packageIdList = (List<PackageItemData>) throwExceptionOnError(data);
        int packageId = packageIdList.size();

        /*Notice: IMPORTANT: packageData contains percent complete for ui, update them 4-5 times max*/
        List<PackageFileData> fileList = new ArrayList<>();
        PackageData packageData = new PackageData();
        packageData.setPackageId(packageId);
        packageData.setProjectId(attributes.getProjectId());
        packageData.setBuildDate(DateTimeUtil.now());
        packageData.setName("building...");

        packageIdList.add(mapper.map(packageData));
        projectDataManager.addData(ProjectFileType.PACKAGE_LIST, packageIdList, projectUser);

        updatePercentComplete(packageData, projectUser, 0, estimateBuiltDate());

        addUploadedFiles(fileList, projectData, projectUser);
        updatePercentComplete(packageData, projectUser, 20, estimateBuiltDate());

        addGeneratedFiles(fileList);
        updatePercentComplete(packageData, projectUser, 50, estimateBuiltDate());

        addVersionedFiles(fileList);
        updatePercentComplete(packageData, projectUser, 75, estimateBuiltDate());

        saveFileList(fileList, packageData, projectUser);
        updatePercentComplete(packageData, projectUser, 100, DateTimeUtil.now());
    }

    private void saveFileList(List<PackageFileData> fileList, PackageData packageData, ProjectUser projectUser) {
        /*TODO: save PackageData with the collected fileList*/
    }

    private void updatePercentComplete(PackageData packageData, ProjectUser projectUser, int percent, Date builtDate) {
        packageData.setComplete(percent);
        packageData.setBuiltDate(builtDate);
        projectDataManager.addData(ProjectFileType.PACKAGE, packageData, projectUser);
    }

    private void addVersionedFiles(List<PackageFileData> fileList) {
        /*TODO: addVersionedFiles*/
    }

    private void addUploadedFiles(List<PackageFileData> fileList, ProjectData projectData, ProjectUser projectUser) throws IOException {
        /*TODO: addUploadedFiles to fileList*/

        /*TODO: get uploadedFileList*/
        Object data = projectDataManager.getData(ProjectFileType.UPLOADED_LIST, projectUser);
        //noinspection unchecked (suppress warning about unchecked)
        /*TODO: List<BinaryFileItemData> packageIdList = (List<BinaryFileItemData>) throwExceptionOnError(data);*/
    }

    private List<PackageFileData> createFileList(PackageData packageData) {
        List<PackageFileData> fileDataList = new ArrayList<>();

        /*TODO: generate file list by ProjectType*/

        return fileDataList;
    }

    private Date estimateBuiltDate() {
        return DateTimeUtil.now();
    }

    private String getDConversFileName(String projectId) {
        return getConfigPath(projectId) + projectId + ".conf";
    }

    private String getDConverterFileName(String projectId, int stepId) {
        return getConfigPath(projectId) + "step" + stepId + ".conf";
    }

    private String getConfigPath(String projectId) {
        return environmentConfigs.getBinaryRootPath() + projectId + "/config/";
    }

    private Object throwExceptionOnError(Object data) throws IOException {
        if (data instanceof Long) {
            throw new IOException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

    private void addGeneratedFiles(List<PackageFileData> fileList) {

        /*TODO: copy DConvers.Test.SourceUT() to here*/



        /*TODO: loop each STEP*/

        /*TODO: create DConvers ConverterConfigFile*/
        /*TODO: read related data within Step Data*/
        /*TODO: set all values same as ConverterConfigFile.loadConfig() do*/
        /*TODO: need config.writeTo binary, collect this into fileList*/
    }

}
