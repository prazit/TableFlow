package com.tflow.tbcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.data.verify.DataVerifier;
import com.tflow.model.data.verify.Verifiers;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.wcmd.IOCommand;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerifyProjectCommand extends IOCommand {
    private Logger log = LoggerFactory.getLogger(BuildPackageCommand.class);

    private KafkaRecordAttributes attributes;

    public VerifyProjectCommand(long offset, String key, Object value, EnvironmentConfigs environmentConfigs, DataManager dataManager) {
        super(offset, key, value, environmentConfigs, dataManager);
    }

    @Override
    public void info(String message, Object... objects) {
        log.info(message, objects);
    }

    @Override
    public void execute() throws UnsupportedOperationException, IOException, ClassNotFoundException, InstantiationException {
        mapper = Mappers.getMapper(PackageMapper.class);
        attributes = (KafkaRecordAttributes) value;
        recordAttributes = Mappers.getMapper(RecordMapper.class).map(attributes);
        ProjectUser projectUser = mapper.map(attributes);

        IssuesData issuesData = new IssuesData();
        ArrayList<IssueData> issueList = new ArrayList<>();
        issuesData.setIssueList(issueList);
        updateIssuesData(0, issuesData, projectUser);

        /*TODO: verifier for project*/
        Object data = getData(ProjectFileType.PROJECT, projectUser.getId());
        ProjectData projectData = (ProjectData) throwExceptionOnError(data);

        DataVerifier verifier = Verifiers.getVerifier(projectData);
        if (!verifier.verify()) {
            issueList.addAll(verifier.getIssueList());
        }

        /*TODO: IMPORTANT: DatabaseVerifier can copy from BuildPackageCommand.throwExceptionOnValidateFail()*/
        /*TODO: 1.verifier for db-list*/
        issueList.addAll(verifyDataList(loadDatabaseDataList()));
        updateIssuesData(10, issuesData, projectUser);

        /*TODO: 2.verifier for sftp-list*/
        issueList.addAll(verifyDataList(loadSFTPDataList()));

        /*TODO: 3.verifier for local-list*/
        issueList.addAll(verifyDataList(loadLocalDataList()));

        /*TODO: 4.verifier for variable-list*/
        issueList.addAll(verifyDataList(loadVariableDataList()));
        updateIssuesData(20, issuesData, projectUser);

        /*TODO: 5.verifier for library-list*/
        issueList.addAll(verifyDataList(loadVersionedDataList(projectData.getType().getCode())));

        /*TODO: 6.verifier for step-list*/
        List<StepData> stepDataList = loadStepDataList();
        verifyDataList(stepDataList);
        updateIssuesData(30, issuesData, projectUser);

        List<DataTableData> dataTableDataList;
        List<TransformTableData> transformTableDataList;
        int dataTableId;
        int transformTableId;
        int stepId;
        int percentCompleteAdder = 70 / stepDataList.size();
        for (StepData stepData : stepDataList) {
            stepId = stepData.getId();

            /*TODO: 7.verifier for data-file-list*/
            issueList.addAll(verifyDataList(loadDataFileDataList(stepId)));

            /*TODO: 8.verifier for data-table-list*/
            dataTableDataList = loadDataTableDataList(stepId);
            issueList.addAll(verifyDataList(dataTableDataList));

            for (DataTableData dataTableData : dataTableDataList) {
                dataTableId = dataTableData.getId();

                /*TODO: 9.verifier for data-column-list*/
                issueList.addAll(verifyDataList(loadDataColumnDataList(stepId, dataTableId)));

                /*TODO: 10.verifier for output-list*/
                issueList.addAll(verifyDataList(loadDataOutputDataList(stepId, dataTableId)));
            }

            /*TODO: 11.verifier for transform-table-list*/
            transformTableDataList = loadTransformTableDataList(stepId);
            issueList.addAll(verifyDataList(transformTableDataList));

            for (TransformTableData transformTableData : transformTableDataList) {
                transformTableId = transformTableData.getId();

                /*TODO: 12.verifier for transform-column-list*/
                issueList.addAll(verifyDataList(loadTransformColumnDataList(stepId, transformTableId)));

                /*TODO: 13.verifier for transform-output-list*/
                issueList.addAll(verifyDataList(loadTransformOutputDataList(stepId, transformTableId)));

                /*TODO: 14.verifier for tranformation-list*/
                issueList.addAll(verifyDataList(loadTransformationDataList(stepId, transformTableId)));
            }

            updateIssuesData(issuesData.getComplete() + percentCompleteAdder, issuesData, projectUser);
        }

        /*TODO: save issueDataList to project*/
        updateIssuesData(100, issuesData, projectUser);

    }

    private List<TableFxData> loadTransformationDataList(int stepId, int transformTableId) {
        List<TableFxData> tableFxDataList = new ArrayList<>();
        /*TODO: load*/
        return tableFxDataList;
    }

    private List<OutputFileData> loadTransformOutputDataList(int stepId, int transformTableId) {
        List<OutputFileData> outputFileDataList = new ArrayList<>();
        /*TODO: load*/
        return outputFileDataList;
    }

    private List<TransformColumnData> loadTransformColumnDataList(int stepId, int transformTableId) {
        List<TransformColumnData> transformColumnDataList = new ArrayList<>();
        /*TODO: load*/
        return transformColumnDataList;
    }

    private List<TransformTableData> loadTransformTableDataList(int stepId) {
        List<TransformTableData> transformTableDataList = new ArrayList<>();
        /*TODO: load*/
        return transformTableDataList;
    }

    private List<OutputFileData> loadDataOutputDataList(int stepId, int dataTableId) {
        List<OutputFileData> outputFileDataList = new ArrayList<>();
        /*TODO: load*/
        return outputFileDataList;
    }

    private List<DataColumnData> loadDataColumnDataList(int stepId, int dataTableId) {
        List<DataColumnData> dataColumnDataList = new ArrayList<>();
        /*TODO: load*/
        return dataColumnDataList;
    }

    private List<DataTableData> loadDataTableDataList(int stepId) {
        List<DataTableData> dataTableDataList = new ArrayList<>();
        /*TODO: load*/
        return dataTableDataList;
    }

    private List<DataFileData> loadDataFileDataList(int stepId) {
        List<DataFileData> dataFileDataList = new ArrayList<>();
        /*TODO: load */
        return dataFileDataList;
    }

    private void updateIssuesData(int percentCompleted, IssuesData issuesData, ProjectUser projectUser) {
        issuesData.setComplete(percentCompleted);
        dataManager.addData(ProjectFileType.ISSUE_LIST, issuesData, projectUser);
        dataManager.waitAllTasks();
    }

    private List<IssueData> verifyDataList(List<? extends TWData> dataList) {
        List<IssueData> issueList = new ArrayList<>();
        for (TWData data : dataList) {
            DataVerifier verifier = Verifiers.getVerifier(data);
            if (!verifier.verify()) {
                issueList.addAll(verifier.getIssueList());
            }
        }
        return issueList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "offset:" + offset +
                ", key:'" + key + '\'' +
                (attributes == null ? "" : ", attributes:" + attributes) +
                '}';
    }
}
