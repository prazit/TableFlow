package com.tflow.tbcmd;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.*;
import com.tflow.model.data.verify.DataVerifier;
import com.tflow.model.data.verify.Verifiers;
import com.tflow.model.mapper.PackageMapper;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.DateTimeUtil;
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
        try {
            verifyProject(issuesData, projectUser);
        } catch (Exception ex) {
            issuesData.setFinished(true);
            issuesData.setFinishDate(DateTimeUtil.now());
            List<IssueData> issueList = issuesData.getIssueList();
            if (issueList.size() == 0) {
                IssueData issueData = new IssueData();

                issueData.setId(1);
                issueData.setType(IssueType.EXCEPTION.name());

                issueData.setStepId(0);
                issueData.setObjectType(ProjectFileType.PROJECT.name());
                issueData.setObjectId("0");
                issueData.setPropertyVar(ex.getMessage());

                issueList.add(issueData);
            }
            updateIssuesData(issuesData.getComplete(), issuesData, projectUser);
            throw ex;
        }

    }

    private void verifyProject(IssuesData issuesData, ProjectUser projectUser) throws ClassNotFoundException, IOException, InstantiationException {
        issuesData.setStartDate(DateTimeUtil.now());

        ArrayList<IssueData> issueList = new ArrayList<>();
        issuesData.setIssueList(issueList);
        updateIssuesData(0, issuesData, projectUser);

        /*0.verifier for project*/
        Object data = getData(ProjectFileType.PROJECT, projectUser.getId());
        ProjectData projectData = (ProjectData) throwExceptionOnError(data);
        Verifiers.getVerifier(projectData).verify(-1, ProjectFileType.PROJECT, issueList);

        /*1.verifier for db-list*/
        verifyDataList(loadDatabaseDataList(), -1, issueList, ProjectFileType.DB);
        updateIssuesData(10, issuesData, projectUser);

        /*2.verifier for sftp-list*/
        verifyDataList(loadSFTPDataList(), -1, issueList, ProjectFileType.SFTP);

        /*3.verifier for local-list*/
        verifyDataList(loadLocalDataList(), -1, issueList, ProjectFileType.LOCAL);

        /*4.verifier for variable-list*/
        verifyDataList(loadVariableDataList(), -1, issueList, ProjectFileType.VARIABLE);
        updateIssuesData(20, issuesData, projectUser);

        /*TODO: 5.verifier for library-list (need to copy from Project-Page.Library-List)*/
        verifyDataList(loadVersionedDataList(projectData.getType().getCode()), -1, issueList, ProjectFileType.VERSIONED);

        /*6.verifier for step-list*/
        List<StepData> stepDataList = loadStepDataList();
        verifyDataList(stepDataList, -1, issueList, ProjectFileType.STEP);
        updateIssuesData(30, issuesData, projectUser);

        List<DataTableData> dataTableDataList;
        List<TransformTableData> transformTableDataList;
        int dataTableId;
        int transformTableId;
        int stepId;
        int percentCompleteAdder = 70 / stepDataList.size();
        for (StepData stepData : stepDataList) {
            stepId = stepData.getId();

            /*7.verifier for data-file-list*/
            verifyDataList(loadDataFileDataList(stepId), stepId, issueList, ProjectFileType.DATA_FILE);

            /*8.verifier for data-table-list*/
            dataTableDataList = loadDataTableDataList(stepId);
            verifyDataList(dataTableDataList, stepId, issueList, ProjectFileType.DATA_TABLE);

            for (DataTableData dataTableData : dataTableDataList) {
                dataTableId = dataTableData.getId();

                /*9.verifier for data-column-list*/
                verifyDataList(loadDataColumnDataList(stepId, dataTableId), stepId, issueList, ProjectFileType.DATA_COLUMN);

                /*10.verifier for output-list*/
                verifyDataList(loadDataOutputDataList(stepId, dataTableId), stepId, issueList, ProjectFileType.DATA_OUTPUT);
            }

            /*11.verifier for transform-table-list*/
            transformTableDataList = loadTransformTableDataList(stepId);
            verifyDataList(transformTableDataList, stepId, issueList, ProjectFileType.TRANSFORM_TABLE);

            for (TransformTableData transformTableData : transformTableDataList) {
                transformTableId = transformTableData.getId();

                /*12.verifier for transform-column-list*/
                verifyDataList(loadTransformColumnDataList(stepId, transformTableId), stepId, issueList, ProjectFileType.TRANSFORM_COLUMN);

                /*13.verifier for transform-output-list*/
                verifyDataList(loadTransformOutputDataList(stepId, transformTableId), stepId, issueList, ProjectFileType.TRANSFORM_OUTPUT);

                /*14.verifier for tranformation-list*/
                verifyDataList(loadTransformationDataList(stepId, transformTableId), stepId, issueList, ProjectFileType.TRANSFORMATION);
            }

            updateIssuesData(issuesData.getComplete() + percentCompleteAdder, issuesData, projectUser);
        }

        /*save complete status*/
        finished(projectUser, issuesData);
    }

    private void finished(ProjectUser projectUser, IssuesData issuesData) {
        issuesData.setFinished(true);
        issuesData.setFinishDate(DateTimeUtil.now());
        updateIssuesData(100, issuesData, projectUser);
    }

    private void verifyDataList(List<? extends TWData> dataList, int stepId, ArrayList<IssueData> issueList, ProjectFileType objectType) {
        boolean isStep = (dataList.size() > 0 && dataList.get(0) instanceof StepData);
        for (TWData data : dataList) {
            DataVerifier verifier = Verifiers.getVerifier(data);
            if (isStep) {
                verifier.verify(((StepData) data).getId(), objectType, issueList);
            } else {
                verifier.verify(stepId, objectType, issueList);
            }
        }
    }

    private void updateIssuesData(int percentCompleted, IssuesData issuesData, ProjectUser projectUser) {
        issuesData.setComplete(percentCompleted);
        dataManager.addData(ProjectFileType.ISSUE_LIST, issuesData, projectUser);
        dataManager.waitAllTasks();
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
