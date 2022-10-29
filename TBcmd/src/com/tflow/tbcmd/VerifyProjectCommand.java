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
        issuesData.setStartDate(DateTimeUtil.now());

        ArrayList<IssueData> issueList = new ArrayList<>();
        issuesData.setIssueList(issueList);
        updateIssuesData(0, issuesData, projectUser);

        /*verifier for project*/
        Object data = getData(ProjectFileType.PROJECT, projectUser.getId());
        ProjectData projectData = (ProjectData) throwExceptionOnError(data);

        DataVerifier verifier = Verifiers.getVerifier(projectData);
        if (!verifier.verify(0, issueList)) {
            issueList.addAll(verifier.getIssueList());
        }

        /*1.verifier for db-list*/
        verifyDataList(loadDatabaseDataList(), 0, issueList);
        updateIssuesData(10, issuesData, projectUser);

        /*2.verifier for sftp-list*/
        verifyDataList(loadSFTPDataList(), 0, issueList);

        /*3.verifier for local-list*/
        verifyDataList(loadLocalDataList(), 0, issueList);

        /*4.verifier for variable-list*/
        verifyDataList(loadVariableDataList(), 0, issueList);
        updateIssuesData(20, issuesData, projectUser);

        /*TODO: 5.verifier for library-list*/
        verifyDataList(loadVersionedDataList(projectData.getType().getCode()), 0, issueList);

        /*6.verifier for step-list*/
        List<StepData> stepDataList = loadStepDataList();
        verifyDataList(stepDataList, 0, issueList);
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
            verifyDataList(loadDataFileDataList(stepId), stepId, issueList);

            /*8.verifier for data-table-list*/
            dataTableDataList = loadDataTableDataList(stepId);
            verifyDataList(dataTableDataList, stepId, issueList);

            for (DataTableData dataTableData : dataTableDataList) {
                dataTableId = dataTableData.getId();

                /*9.verifier for data-column-list*/
                verifyDataList(loadDataColumnDataList(stepId, dataTableId), stepId, issueList);

                /*10.verifier for output-list*/
                verifyDataList(loadDataOutputDataList(stepId, dataTableId), stepId, issueList);
            }

            /*11.verifier for transform-table-list*/
            transformTableDataList = loadTransformTableDataList(stepId);
            verifyDataList(transformTableDataList, stepId, issueList);

            for (TransformTableData transformTableData : transformTableDataList) {
                transformTableId = transformTableData.getId();

                /*12.verifier for transform-column-list*/
                verifyDataList(loadTransformColumnDataList(stepId, transformTableId), stepId, issueList);

                /*13.verifier for transform-output-list*/
                verifyDataList(loadTransformOutputDataList(stepId, transformTableId), stepId, issueList);

                /*14.verifier for tranformation-list*/
                verifyDataList(loadTransformationDataList(stepId, transformTableId), stepId, issueList);
            }

            updateIssuesData(issuesData.getComplete() + percentCompleteAdder, issuesData, projectUser);
        }

        /*save complete status*/
        issuesData.setFinishDate(DateTimeUtil.now());
        updateIssuesData(100, issuesData, projectUser);

    }

    private void updateIssuesData(int percentCompleted, IssuesData issuesData, ProjectUser projectUser) {
        issuesData.setComplete(percentCompleted);
        dataManager.addData(ProjectFileType.ISSUE_LIST, issuesData, projectUser);
        dataManager.waitAllTasks();
    }

    private void verifyDataList(List<? extends TWData> dataList, int stepId, ArrayList<IssueData> issueList) {
        boolean isStep = (dataList.size() > 0 && dataList.get(0) instanceof StepData);
        for (TWData data : dataList) {
            DataVerifier verifier = Verifiers.getVerifier(data);
            if (isStep) {
                if (!verifier.verify(((StepData) data).getId(), issueList)) {
                    issueList.addAll(verifier.getIssueList());
                }
                continue;
            }
            if (!verifier.verify(stepId, issueList)) {
                issueList.addAll(verifier.getIssueList());
            }
        }
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
