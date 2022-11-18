package com.tflow.model.editor;

import com.tflow.kafka.*;
import com.tflow.model.data.*;
import com.tflow.model.data.query.*;
import com.tflow.model.data.verify.Verifiers;
import com.tflow.model.editor.cmd.AddProject;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryFilter;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.model.editor.view.UploadedFileView;
import com.tflow.model.editor.view.VersionedFile;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.system.Environment;
import com.tflow.util.DateTimeUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProjectManager {

    private Logger log = LoggerFactory.getLogger(ProjectManager.class);

    private EnvironmentConfigs environmentConfigs;
    private String buildPackageTopic;

    public ProjectManager(Environment environment) {
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());
    }

    /**
     * Notice: IMPORTANT: when selectable-object-type is added, need to add script to collect them here.
     */
    public void collectSelectableTo(Map<String, Selectable> map, List<Selectable> selectableList) {
        for (Selectable selectable : selectableList) {
            map.put(selectable.getSelectableId(), selectable);
            if (selectable instanceof DataTable) {
                DataTable dt = (DataTable) selectable;

                for (DataColumn column : dt.getColumnList()) {
                    map.put(column.getSelectableId(), column);
                }

                for (DataFile output : dt.getOutputList()) {
                    map.put(output.getSelectableId(), output);
                }

                if (selectable instanceof TransformTable) {
                    TransformTable tt = (TransformTable) selectable;
                    for (TableFx tableFx : tt.getFxList()) {
                        map.put(tableFx.getSelectableId(), tableFx);
                    }
                }

            }
        }
    }

    public List<Selectable> getSelectableList(List<Floor> floorList) {
        List<Selectable> selectableList = new ArrayList<>();
        for (Floor floor : floorList) {
            selectableList.addAll(collectSelectableRoom(floor.getRoomList()));
        }
        return selectableList;
    }

    public List<Selectable> collectSelectableRoom(List<Room> roomList) {
        List<Selectable> selectableList = new ArrayList<>();
        for (Room room : roomList) {
            if (room instanceof Selectable) {
                selectableList.add((Selectable) room);
            }
        }
        return selectableList;
    }

    public void addSeletable(Selectable selectable, Project project) {
        Map<String, Selectable> selectableMap = project.getActiveStep().getSelectableMap();
        selectableMap.put(selectable.getSelectableId(), selectable);
    }

    private Producer<String, Object> createProducer() {
        /*TODO: need to load producer configuration*/
        buildPackageTopic = KafkaTopics.PROJECT_BUILD.getTopic();
        java.util.Properties props = new Properties();
        props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("request.timeout.ms", 30000);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer.encoding", "UTF-8");
        props.put("value.serializer", environmentConfigs.getKafkaSerializer());
        return new KafkaProducer<>(props);
    }

    public String getNewProjectId(int groupId, Workspace workspace, DataManager dataManager) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(workspace.getProject());
        String projectGroupId = String.valueOf(groupId);

        ProjectData projectData = (ProjectData) throwExceptionOnError(dataManager.getData(ProjectFileType.PROJECT, projectUser, projectGroupId));
        LoggerFactory.getLogger(AddProject.class).debug("projectData = {}", projectData);

        return projectData.getId();
    }

    public void saveProjectAs(String newProjectId, Project project) {
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

        String oldId = project.getId();
        project.setId(newProjectId);
        ProjectData projectData = mapper.map(project);
        ProjectUser projectUser = mapper.toProjectUser(project);

        dataManager.addData(ProjectFileType.PROJECT, projectData, projectUser, newProjectId);
        dataManager.addData(ProjectFileType.PACKAGE_LIST, new ArrayList(), projectUser);
        dataManager.addData(ProjectFileType.UPLOADED_LIST, new ArrayList(), projectUser);
        dataManager.addData(ProjectFileType.GENERATED_LIST, new ArrayList(), projectUser);

        Map<Integer, Database> databaseMap = project.getDatabaseMap();
        dataManager.addData(ProjectFileType.DB_LIST, mapper.fromMap(databaseMap), projectUser, "1");
        for (Database database : databaseMap.values()) {
            dataManager.addData(ProjectFileType.DB, mapper.map(database), projectUser, database.getId());
        }

        Map<Integer, SFTP> sftpMap = project.getSftpMap();
        dataManager.addData(ProjectFileType.SFTP_LIST, mapper.fromMap(sftpMap), projectUser, "2");
        for (SFTP sftp : sftpMap.values()) {
            dataManager.addData(ProjectFileType.SFTP, mapper.map(sftp), projectUser, sftp.getId());
        }

        Map<Integer, Local> localMap = project.getLocalMap();
        dataManager.addData(ProjectFileType.LOCAL_LIST, mapper.fromMap(localMap), projectUser, "3");
        for (Local local : localMap.values()) {
            dataManager.addData(ProjectFileType.LOCAL, mapper.map(local), projectUser, local.getId());
        }

        Map<String, Variable> variableMap = project.getVariableMap();
        dataManager.addData(ProjectFileType.VARIABLE_LIST, mapper.fromVarMap(variableMap), projectUser, "3");
        for (Variable variable : variableMap.values()) {
            dataManager.addData(ProjectFileType.VARIABLE, mapper.map(variable), projectUser, variable.getName());
        }

        dataManager.addData(ProjectFileType.STEP_LIST, mapper.fromStepList(project.getStepList()), projectUser, "4");
        for (Step step : project.getStepList()) {
            saveStep(step, project);
        }

        projectUser.setId(oldId);
    }

    public void saveStep(Step step, Project project) {
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);

        int stepId = step.getId();
        dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);

        /*add each tower in step*/
        List<Tower> towerList = Arrays.asList(step.getDataTower(), step.getTransformTower(), step.getOutputTower());
        for (Tower tower : towerList) {
            dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), stepId);
        }

        /*add data-source-selector-list*/
        List<DataSourceSelector> dataSourceSelectorList = step.getDataSourceSelectorList();
        dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, mapper.fromDataSourceSelectorList(dataSourceSelectorList), projectUser, 0, stepId);

        /*add data-source-selector*/
        for (DataSourceSelector dataSourceSelector : dataSourceSelectorList) {
            dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map(dataSourceSelector), projectUser, dataSourceSelector.getId(), stepId);
        }

        /*add data-file-list*/
        List<DataFile> dataFileList = step.getFileList();
        dataManager.addData(ProjectFileType.DATA_FILE_LIST, mapper.fromDataFileList(dataFileList), projectUser, 0, stepId);

        /*add data-file*/
        for (DataFile dataFile : dataFileList) {
            dataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), projectUser, dataFile.getId(), stepId);
        }

        /*add data-table-list*/
        List<DataTable> dataList = step.getDataList();
        dataManager.addData(ProjectFileType.DATA_TABLE_LIST, mapper.fromDataTableList(dataList), projectUser, 1, stepId);

        /*add each data-table in data-table-list*/
        for (DataTable dataTable : dataList) {
            int dataTableId = dataTable.getId();

            /*add data-table*/
            dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map(dataTable), projectUser, dataTableId, stepId, dataTableId);

            /*add column-list*/
            List<DataColumn> columnList = dataTable.getColumnList();
            dataManager.addData(ProjectFileType.DATA_COLUMN_LIST, mapper.fromDataColumnList(columnList), projectUser, 2, stepId, dataTableId);

            /*add each column in column-list*/
            for (DataColumn dataColumn : columnList) {
                dataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), projectUser, dataColumn.getId(), stepId, dataTableId);
            }

            /*add output-list*/
            List<OutputFile> outputList = dataTable.getOutputList();
            dataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), projectUser, 2, stepId, dataTableId);

            /*add each output in output-list*/
            for (OutputFile output : outputList) {
                dataManager.addData(ProjectFileType.DATA_OUTPUT, mapper.map(output), projectUser, output.getId(), stepId, dataTableId);
            }
        }

        /*add transform-table-list*/
        List<TransformTable> transformList = step.getTransformList();
        dataManager.addData(ProjectFileType.TRANSFORM_TABLE_LIST, mapper.fromTransformTableList(transformList), projectUser, 4, stepId);

        /*add each transform-table in transform-table-list*/
        for (TransformTable transformTable : transformList) {
            int transformTableId = transformTable.getId();

            /*add Transform-table*/
            dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map(transformTable), projectUser, transformTableId, stepId, 0, transformTableId);

            /*add transform-column-list*/
            List<DataColumn> columnList = transformTable.getColumnList();
            dataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), projectUser, 5, stepId, 0, transformTableId);

            /*add each transform-column in transform-column-list*/
            for (DataColumn dataColumn : columnList) {
                dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) dataColumn), projectUser, dataColumn.getId(), stepId, 0, transformTableId);
            }

            /*add transform-output-list*/
            List<OutputFile> outputList = transformTable.getOutputList();
            dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT_LIST, mapper.fromOutputFileList(outputList), projectUser, 6, stepId, 0, transformTableId);

            /*add each transform-output in transform-output-list*/
            for (OutputFile output : outputList) {
                dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT, mapper.map(output), projectUser, output.getId(), stepId, 0, transformTableId);
            }

            /*add tranformation-list*/
            List<TableFx> fxList = transformTable.getFxList();
            dataManager.addData(ProjectFileType.TRANSFORMATION_LIST, mapper.fromTableFxList(fxList), projectUser, 7, stepId, 0, transformTableId);

            /*add each tranformation in tranformation-list*/
            for (TableFx tableFx : fxList) {
                dataManager.addData(ProjectFileType.TRANSFORMATION, mapper.map(tableFx), projectUser, tableFx.getId(), stepId, 0, transformTableId);
            }
        }

        /*add line-list at the end*/
        List<Line> lineList = step.getLineList();
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(lineList), projectUser, 8, stepId);

        /*add each line in line-list*/
        for (Line line : lineList) {
            dataManager.addData(ProjectFileType.LINE, mapper.map(line), projectUser, line.getId(), stepId);
        }

        // update Step List
        dataManager.addData(ProjectFileType.STEP_LIST, mapper.fromStepList(project.getStepList()), projectUser, "");

        // update Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, projectUser.getId());
    }

    public ProjectGroupList loadGroupList(Workspace workspace) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = workspace.getDataManager();

        ProjectUser projectUser = new ProjectUser();
        projectUser.setUserId(workspace.getUser().getId());
        projectUser.setClientId(workspace.getClient().getId());

        Object data = dataManager.getData(ProjectFileType.GROUP_LIST, projectUser);
        GroupListData groupListData = (GroupListData) throwExceptionOnError(data);

        return mapper.map(groupListData);
    }

    public ProjectGroup loadProjectGroup(Workspace workspace, int groupId) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = workspace.getDataManager();

        ProjectUser projectUser = new ProjectUser();
        projectUser.setUserId(workspace.getUser().getId());
        projectUser.setClientId(workspace.getClient().getId());

        Object data = dataManager.getData(ProjectFileType.GROUP, projectUser, groupId);
        GroupData groupData = (GroupData) throwExceptionOnError(data);

        return mapper.map(groupData);
    }

    /**
     * TODO: need to support open new project from template (projectId < 0)
     **/
    @SuppressWarnings("unchecked")
    public Project loadProject(Workspace workspace, String openProjectId) throws ClassCastException, ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = new ProjectUser();
        projectUser.setId(openProjectId);
        projectUser.setUserId(workspace.getUser().getId());
        projectUser.setClientId(workspace.getClient().getId());

        /*get project, to know the project is not edit by another */
        DataManager dataManager = workspace.getDataManager();
        Object data = dataManager.getData(ProjectFileType.PROJECT, projectUser, projectUser.getId());
        Project project = mapper.map((ProjectData) throwExceptionOnError(data));
        project.setOwner(workspace);
        project.setDataManager(dataManager);
        project.setManager(this);

        /*get db-list*/
        data = dataManager.getData(ProjectFileType.DB_LIST, projectUser, "1");
        List<Integer> databaseIdList = (List) throwExceptionOnError(data);
        Map<Integer, Database> databaseMap = new HashMap<>();
        project.setDatabaseMap(databaseMap);

        /*get each db in db-list*/
        for (Integer id : databaseIdList) {
            data = dataManager.getData(ProjectFileType.DB, projectUser, String.valueOf(id));
            databaseMap.put(id, mapper.map((DatabaseData) throwExceptionOnError(data)));
        }

        /*get sftp-list*/
        data = dataManager.getData(ProjectFileType.SFTP_LIST, projectUser, "2");
        List<Integer> sftpIdList = (List) throwExceptionOnError(data);
        Map<Integer, SFTP> sftpMap = new HashMap<>();
        project.setSftpMap(sftpMap);

        /*get each sftp in sftp-list*/
        for (Integer id : sftpIdList) {
            data = dataManager.getData(ProjectFileType.SFTP, projectUser, String.valueOf(id));
            sftpMap.put(id, mapper.map((SFTPData) throwExceptionOnError(data)));
        }

        /*get local-list*/
        data = dataManager.getData(ProjectFileType.LOCAL_LIST, projectUser, "3");
        List<Integer> localIdList = (List) throwExceptionOnError(data);
        Map<Integer, Local> localMap = new HashMap<>();
        project.setLocalMap(localMap);

        /*get each local in local-list*/
        for (Integer id : localIdList) {
            data = dataManager.getData(ProjectFileType.LOCAL, projectUser, String.valueOf(id));
            localMap.put(id, mapper.map((LocalData) throwExceptionOnError(data)));
        }

        /*get system-variables before user-variables*/
        Map<String, Variable> varMap = new HashMap<>();
        Variable variable;
        int vIndex = 1;
        List<SystemVariable> systemVariableList = Arrays.asList(SystemVariable.values());
        systemVariableList.sort(Comparator.comparing(SystemVariable::name));
        for (SystemVariable systemVariable : systemVariableList) {
            variable = new Variable(vIndex++, VariableType.SYSTEM, systemVariable.name(), systemVariable.getDescription());
            varMap.put(systemVariable.name(), variable);
        }

        /*get variable-list (user-variable)*/
        data = dataManager.getData(ProjectFileType.VARIABLE_LIST, projectUser, "4");
        List<Integer> varIdList = (List<Integer>) throwExceptionOnError(data);
        project.setVariableMap(varMap);

        /*get each variable in variable-list*/
        for (Integer id : varIdList) {
            data = dataManager.getData(ProjectFileType.VARIABLE, projectUser, id);
            variable = mapper.map((VariableData) throwExceptionOnError(data));
            variable.setType(VariableType.USER);
            variable.setIndex(vIndex++);
            varMap.put(variable.getName(), variable);
        }

        /*get step-list*/
        data = dataManager.getData(ProjectFileType.STEP_LIST, projectUser, "5");
        List<ItemData> itemDataList = (List<ItemData>) throwExceptionOnError(data);
        project.setStepList(mapper.toStepList(itemDataList));

        workspace.setProject(project);
        return project;
    }

    @SuppressWarnings("unchecked")
    public Step loadStep(Project project, int stepIndex) throws Exception {
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);

        List<Step> stepList = project.getStepList();
        Step stepModel = stepList.get(stepIndex);
        int stepId = stepModel.getId();

        /*get step*/
        Object data = dataManager.getData(ProjectFileType.STEP, projectUser, stepId, stepId);
        StepData stepData = (StepData) throwExceptionOnError(data);
        Step step = mapper.map(stepData);
        step.setOwner(project);

        /*active object need selectableId*/
        SelectableIdOnly selectableIdOnly = new SelectableIdOnly(stepData.getActiveObject());
        step.setActiveObject(selectableIdOnly);

        /*get each tower in step*/
        List<Integer> towerIdList = Arrays.asList(step.getDataTower().getId(), step.getTransformTower().getId(), step.getOutputTower().getId());
        List<Tower> towerList = new ArrayList<>();
        for (Integer towerId : towerIdList) {
            data = dataManager.getData(ProjectFileType.TOWER, projectUser, towerId, stepId);
            Tower tower = mapper.map((TowerData) throwExceptionOnError(data));
            tower.setOwner(step);
            towerList.add(tower);
            fillTower(tower);
        }
        step.setDataTower(towerList.get(0));
        step.setTransformTower(towerList.get(1));
        step.setOutputTower(towerList.get(2));

        /*get data-source-selector-list*/
        data = dataManager.getData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, projectUser, 1, stepId);
        List<Integer> dataSourceSelectorIdList = (List) throwExceptionOnError(data);
        List<DataSourceSelector> dataSourceSelectorList = new ArrayList<>();
        step.setDataSourceSelectorList(dataSourceSelectorList);

        /*get data-source-selector*/
        for (Integer dataSourceSelectorId : dataSourceSelectorIdList) {
            data = dataManager.getData(ProjectFileType.DATA_SOURCE_SELECTOR, projectUser, dataSourceSelectorId, stepId);
            DataSourceSelector dataSourceSelector = mapper.map((DataSourceSelectorData) throwExceptionOnError(data));
            dataSourceSelector.setOwner(step);
            dataSourceSelectorList.add(dataSourceSelector);
            step.getDataTower().setRoom(dataSourceSelector.getFloorIndex(), dataSourceSelector.getRoomIndex(), dataSourceSelector);
            dataSourceSelector.createPlugListeners();
        }

        /*get data-file-list*/
        data = dataManager.getData(ProjectFileType.DATA_FILE_LIST, projectUser, 1, stepId);
        List<Integer> dataFileIdList = (List) throwExceptionOnError(data);
        List<DataFile> dataFileList = new ArrayList<>();
        step.setFileList(dataFileList);

        /*get data-file*/
        for (Integer dataFileId : dataFileIdList) {
            data = dataManager.getData(ProjectFileType.DATA_FILE, projectUser, dataFileId, stepId);
            DataFile dataFile = mapper.map((DataFileData) throwExceptionOnError(data));
            dataFileList.add(dataFile);
            step.getDataTower().setRoom(dataFile.getFloorIndex(), dataFile.getRoomIndex(), dataFile);
        }

        /*get data-table-list*/
        data = dataManager.getData(ProjectFileType.DATA_TABLE_LIST, projectUser, 1, stepId);
        List<Integer> dataTableIdList = (List) throwExceptionOnError(data);
        List<DataTable> dataTableList = new ArrayList<>();
        step.setDataList(dataTableList);

        /*get each data-table in data-table-list*/
        for (Integer dataTableId : dataTableIdList) {
            data = dataManager.getData(ProjectFileType.DATA_TABLE, projectUser, dataTableId, stepId, dataTableId);
            DataTable dataTable = mapper.map((DataTableData) throwExceptionOnError(data));
            dataTable.setOwner(step);
            dataTableList.add(dataTable);
            step.getDataTower().setRoom(dataTable.getFloorIndex(), dataTable.getRoomIndex(), dataTable);
            dataTable.createPlugListeners();

            /*get data-file in data-table by id*/
            DataFile dataFile = step.getFile(dataTable.getDataFile().getId());
            dataTable.setDataFile(dataFile);
            dataFile.setOwner(dataTable);

            /*get column-list*/
            data = dataManager.getData(ProjectFileType.DATA_COLUMN_LIST, projectUser, 1, stepId, dataTableId);
            List<Integer> columnIdList = (List) throwExceptionOnError(data);
            List<DataColumn> columnList = new ArrayList<>();
            dataTable.setColumnList(columnList);

            /*get each column in column-list*/
            DataColumn dataColumn;
            for (Integer columnId : columnIdList) {
                data = dataManager.getData(ProjectFileType.DATA_COLUMN, projectUser, columnId, stepId, dataTableId);
                dataColumn = mapper.map((DataColumnData) throwExceptionOnError(data));
                dataColumn.setOwner(dataTable);
                columnList.add(dataColumn);
                dataColumn.createPlugListeners();
            }

            /*get output-list*/
            data = dataManager.getData(ProjectFileType.DATA_OUTPUT_LIST, projectUser, 1, stepId, dataTableId);
            List<Integer> outputIdList = (List) throwExceptionOnError(data);
            List<OutputFile> outputList = new ArrayList<>();
            dataTable.setOutputList(outputList);

            /*get each output in output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = dataManager.getData(ProjectFileType.DATA_OUTPUT, projectUser, outputId, stepId, dataTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(dataTable);
                outputFile.selected();
                outputList.add(outputFile);
            }

        }// end of for:DataTableIdList

        /*get transform-table-list*/
        data = dataManager.getData(ProjectFileType.TRANSFORM_TABLE_LIST, projectUser, 9, stepId);
        List<Integer> transformTableIdList = (List) throwExceptionOnError(data);
        List<TransformTable> transformTableList = new ArrayList<>();
        step.setTransformList(transformTableList);

        /*get each transform-table in transform-table-list*/
        for (Integer transformTableId : transformTableIdList) {
            data = dataManager.getData(ProjectFileType.TRANSFORM_TABLE, projectUser, transformTableId, stepId, 0, transformTableId);
            TransformTable transformTable = mapper.map((TransformTableData) throwExceptionOnError(data));
            transformTable.setOwner(step);
            transformTableList.add(transformTable);
            step.getTransformTower().setRoom(transformTable.getFloorIndex(), transformTable.getRoomIndex(), transformTable);
            transformTable.createPlugListeners();

            /*get transform-column-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> columnIdList = (List) throwExceptionOnError(data);
            List<DataColumn> columnList = new ArrayList<>();
            transformTable.setColumnList(columnList);

            /*get each transform-column in transform-column-list*/
            TransformColumn transformColumn;
            for (Integer columnId : columnIdList) {
                data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN, projectUser, columnId, stepId, 0, transformTableId);
                transformColumn = mapper.map((TransformColumnData) throwExceptionOnError(data));
                transformColumn.setOwner(transformTable);
                columnList.add(transformColumn);
                transformColumn.createPlugListeners();
            }

            /*get transform-output-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> outputIdList = (List) throwExceptionOnError(data);
            List<OutputFile> outputList = new ArrayList<>();
            transformTable.setOutputList(outputList);

            /*get each transform-output in transform-output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT, projectUser, outputId, stepId, 0, transformTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(transformTable);
                outputFile.createOwnerEventHandlers();
                outputFile.selected();
                outputFile.getProperties().initPropertyMap(outputFile.getPropertyMap());
                outputList.add(outputFile);
            }

            /*get tranformation-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORMATION_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> fxIdList = (List) throwExceptionOnError(data);
            List<TableFx> fxList = new ArrayList<>();
            transformTable.setFxList(fxList);

            /*get each tranformation in tranformation-list*/
            /*need Transformmation Model, find "dataManager.addData(ProjectFileType.TRANSFORMATION" then use Mapper*/
            for (Integer fxId : fxIdList) {
                data = dataManager.getData(ProjectFileType.TRANSFORMATION, projectUser, fxId, stepId, 0, transformTableId);
                TableFx tableFx = mapper.map((TableFxData) throwExceptionOnError(data));
                tableFx.setOwner(transformTable);
                fxList.add(tableFx);
            }

        }

        /*get line-list at the end*/
        data = dataManager.getData(ProjectFileType.LINE_LIST, projectUser, stepId, stepId);
        List<Integer> lineIdList = (List) throwExceptionOnError(data);
        List<Line> lineList = new ArrayList<>();
        step.setLineList(lineList);

        /*get each line in line-list*/
        for (Integer lineId : lineIdList) {
            data = dataManager.getData(ProjectFileType.LINE, projectUser, lineId, stepId);
            Line line = mapper.map((LineData) throwExceptionOnError(data));
            lineList.add(line);
        }

        return step;
    }

    public List<Item> loadPackageList(Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        Object data = dataManager.getData(ProjectFileType.PACKAGE_LIST, projectUser);
        List<ItemData> packageItemDataList = (List<ItemData>) throwExceptionOnError(data);

        return mapper.toItemList(packageItemDataList);
    }

    public Package loadPackage(int packageId, Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        Object data = dataManager.getData(ProjectFileType.PACKAGE, projectUser, packageId);
        PackageData packageData = (PackageData) throwExceptionOnError(data);

        return mapper.map(packageData);
    }

    public BinaryFile loadPackaged(int packageId, Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        Object data = dataManager.getData(ProjectFileType.PACKAGED, projectUser, packageId);
        BinaryFileData binaryFileData = (BinaryFileData) throwExceptionOnError(data);

        return mapper.map(binaryFileData);
    }

    public Query loadQuery(int queryId, Project project) throws ProjectDataException {
        int stepId = project.getActiveStep().getId();

        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        String childId = String.valueOf(queryId);
        Object data = dataManager.getData(ProjectFileType.QUERY, projectUser, queryId, stepId, childId);
        Query query = mapper.map((QueryData) throwExceptionOnError(data));

        /*QUETY_TOWER*/
        data = dataManager.getData(ProjectFileType.QUERY_TOWER, projectUser, query.getTower().getId(), stepId, childId);
        Tower tower = mapper.map((TowerData) throwExceptionOnError(data));
        query.setTower(tower);

        /*create each floor in tower*/
        fillTower(tower);

        /*QUERY_TABLE_LIST*/
        data = dataManager.getData(ProjectFileType.QUERY_TABLE_LIST, projectUser, queryId, stepId, childId);
        List<Integer> idList = (List) throwExceptionOnError(data);

        /*QUERY_TABLE*/
        LinkedHashMap<Integer, QueryTable> tableMap = new LinkedHashMap<>();
        List<QueryTable> tableList = query.getTableList();
        QueryTableData queryTableData;
        QueryTable queryTable;
        QueryColumnData queryColumnData;
        for (Integer tableId : idList) {
            data = dataManager.getData(ProjectFileType.QUERY_TABLE, projectUser, tableId, stepId, childId);
            queryTableData = (QueryTableData) throwExceptionOnError(data);
            queryTable = mapper.map(queryTableData);
            tableList.add(queryTable);
            tower.setRoom(queryTable.getFloorIndex(), queryTable.getRoomIndex(), queryTable);

            for (QueryColumn queryColumn : queryTable.getColumnList()) {
                queryColumn.setOwner(queryTable);
            }

            tableMap.put(queryTable.getId(), queryTable);
        }

        /*selected columns need owner*/
        int tableId;
        for (QueryColumn queryColumn : query.getColumnList()) {
            tableId = queryColumn.getOwner().getId();
            if(tableId < 0) continue;

            queryTable = tableMap.get(tableId);
            if(queryTable!=null) queryColumn.setOwner(queryTable);
        }

        /*QUERY_FILTER_LIST*/
        data = dataManager.getData(ProjectFileType.QUERY_FILTER_LIST, projectUser, queryId, stepId, childId);
        idList = (List) throwExceptionOnError(data);

        /*QUERY_FILTER*/
        List<QueryFilter> filterList = query.getFilterList();
        QueryFilterData queryFilterData;
        for (Integer filterId : idList) {
            data = dataManager.getData(ProjectFileType.QUERY_FILTER, projectUser, filterId, stepId, childId);
            queryFilterData = (QueryFilterData) throwExceptionOnError(data);
            filterList.add(mapper.map(queryFilterData));
        }

        /*QUERY_SORT_LIST*/
        data = dataManager.getData(ProjectFileType.QUERY_SORT_LIST, projectUser, queryId, stepId, childId);
        idList = (List) throwExceptionOnError(data);

        /*QUERY_SORT*/
        QuerySortData querySortData;
        for (Integer sortId : idList) {
            data = dataManager.getData(ProjectFileType.QUERY_SORT, projectUser, sortId, stepId, childId);
            querySortData = (QuerySortData) throwExceptionOnError(data);
            filterList.add(mapper.map(querySortData));
        }

        return query;
    }

    /**
     * Create floors and rooms for Empty-Tower after load data.
     */
    private void fillTower(Tower tower) {
        int roomsOnAFloor = tower.getRoomsOnAFloor();
        List<Floor> floorList = tower.getFloorList();
        for (int floorIndex = 0; floorIndex < floorList.size(); floorIndex++) {
            Floor floor = floorList.get(floorIndex);
            floor.setIndex(floorIndex);
            floor.setTower(tower);

            /*create each room in a floor*/
            List<Room> roomList = floor.getRoomList();
            roomList.clear();
            for (int index = 0; index < roomsOnAFloor; index++) {
                roomList.add(new EmptyRoom(index, floor));
            }
        }
    }

    /**
     * After call buildPackage at least 2 seconds you need to get PackageData for complete-status.
     *
     * @return mockup data with real-build-date to show for building...
     */
    public Package buildPackage(Project project, Package rebuild) {
        Date buildDate = DateTimeUtil.now();
        Producer<String, Object> producer = createProducer();

        Workspace workspace = project.getOwner();
        long transactionId = 0;
        try {
            transactionId = project.getDataManager().newTransactionId();
        } catch (InterruptedException ex) {
            log.error("{}", ex.getMessage());
            log.trace("", ex);
            return null;
        }

        /*create build Message for TBcmd*/
        KafkaRecordAttributes kafkaRecordAttributes = new KafkaRecordAttributes();
        kafkaRecordAttributes.setRecordId(rebuild == null ? null : String.valueOf(rebuild.getId()));
        kafkaRecordAttributes.setTransactionId(transactionId);
        kafkaRecordAttributes.setProjectId(project.getId());
        kafkaRecordAttributes.setUserId(workspace.getUser().getId());
        kafkaRecordAttributes.setClientId(workspace.getClient().getId());
        kafkaRecordAttributes.setModifiedDate(buildDate);
        log.debug("Outgoing message: build(attributes:{})", kafkaRecordAttributes);

        /*send message*/
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(buildPackageTopic, "build", kafkaRecordAttributes));

        boolean success = isSuccess(future);
        producer.close();
        if (!success) return null;

        Package activePackage = rebuild == null ? new Package() : rebuild;
        activePackage.setId(-1);
        activePackage.setName("building...");
        activePackage.setBuildDate(buildDate);
        activePackage.setComplete(0);
        activePackage.setFinished(false);
        activePackage.setBuiltDate(null);
        return activePackage;
    }

    public List<VersionedFile> loadVersionedList(Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        List<VersionedFile> versionedFileList = new ArrayList<>();
        VersionedFile versionedFile;
        int index = 1;
        ProjectUser projectUser = mapper.toProjectUser(project);

        Object data = dataManager.getData(ProjectFileType.VERSIONED_LIST, projectUser);
        List<VersionedFileData> versionedList;
        try {
            versionedList = (List<VersionedFileData>) throwExceptionOnError(data);
        } catch (ProjectDataException ex) {
            if (ex.getMessage().contains(KafkaErrorCode.DATA_FILE_NOT_FOUND.name())) {
                versionedList = new ArrayList<>();
            } else {
                throw ex;
            }
        }

        fullVersionedList(project.getType(), versionedList);
        for (VersionedFileData versioned : versionedList) {
            versionedFile = mapper.map(versioned);
            versionedFile.setIndex(index++);
            versionedFileList.add(versionedFile);
        }

        return versionedFileList;
    }

    private void fullVersionedList(ProjectType projectType, List<VersionedFileData> versionedList) {
        StringBuilder existingBuilder = new StringBuilder();
        for (VersionedFileData item : versionedList) existingBuilder.append(",").append(item.getId());
        String existing = existingBuilder.append(",").toString();

        /*create full list*/
        for (Versioned versioned : Versioned.getVersionedList(projectType)) {
            if (!existing.contains("," + versioned.name() + ",")) {
                VersionedFileData versionedFileData = new VersionedFileData();
                versionedFileData.setId(versioned.name());
                versionedFileData.setName("");
                versionedList.add(versionedFileData);
            }
        }

        versionedList.sort(Comparator.comparing(VersionedFileData::getId));
    }

    public List<UploadedFileView> loadUploadedList(Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        List<UploadedFileView> uploadedFileList = new ArrayList<>();
        UploadedFileView uploadedFile;
        int index = 1;
        ProjectUser projectUser = mapper.toProjectUser(project);

        for (Step step : project.getStepList()) {
            Object data = dataManager.getData(ProjectFileType.DATA_FILE_LIST, projectUser, 0, step.getId());
            List<Integer> dataFileIdList = (List<Integer>) throwExceptionOnError(data);

            for (Integer dataFileId : dataFileIdList) {
                data = dataManager.getData(ProjectFileType.DATA_FILE, projectUser, dataFileId, step.getId());
                DataFileData dataFileData = (DataFileData) throwExceptionOnError(data);
                DataFileType dataFileType = DataFileType.valueOf(dataFileData.getType());
                if (dataFileType.getAllowTypes() == null) continue;

                uploadedFile = new UploadedFileView();
                uploadedFile.setIndex(index++);
                uploadedFile.setStepName(step.getName());
                uploadedFile.setDataFileType(dataFileType);
                uploadedFile.setFileName(dataFileData.getName());
                uploadedFileList.add(uploadedFile);
            }
        }

        return uploadedFileList;
    }

    public BinaryFile loadUploaded(int fileId, Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        Object data = dataManager.getData(ProjectFileType.UPLOADED, projectUser, fileId);
        BinaryFileData binaryFileData = (BinaryFileData) throwExceptionOnError(data);

        return mapper.map(binaryFileData);
    }

    public Issues loadIssues(Project project) throws ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();

        ProjectUser projectUser = mapper.toProjectUser(project);
        Object data = dataManager.getData(ProjectFileType.ISSUE_LIST, projectUser);
        IssuesData issuesData = (IssuesData) throwExceptionOnError(data);

        return mapper.map(issuesData);
    }

    private boolean isSuccess(Future<RecordMetadata> future) {
        while (!future.isDone()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                /*nothing*/
            }
        }

        log.debug("Future: isDone={}, isCancelled={}", future.isDone(), future.isCancelled());
        boolean result = true;
        try {
            RecordMetadata recordMetadata = future.get();
            log.debug("RecordMetadata: {}", recordMetadata);
        } catch (InterruptedException ex) {
            log.warn("InterruptedException: ", ex);
            result = false;
        } catch (ExecutionException ex) {
            log.warn("ExecutionException: ", ex);
            result = false;
        }
        return result;
    }

    public boolean verifyProject(Project project) {
        Date buildDate = DateTimeUtil.now();
        Producer<String, Object> producer = createProducer();

        Workspace workspace = project.getOwner();
        long transactionId = 0;
        try {
            transactionId = project.getDataManager().newTransactionId();
        } catch (InterruptedException ex) {
            log.error("{}", ex.getMessage());
            log.trace("", ex);
            return false;
        }

        /*create build Message for TBcmd*/
        KafkaRecordAttributes kafkaRecordAttributes = new KafkaRecordAttributes();
        kafkaRecordAttributes.setRecordId(project.getId());
        kafkaRecordAttributes.setTransactionId(transactionId);
        kafkaRecordAttributes.setProjectId(project.getId());
        kafkaRecordAttributes.setUserId(workspace.getUser().getId());
        kafkaRecordAttributes.setClientId(workspace.getClient().getId());
        kafkaRecordAttributes.setModifiedDate(buildDate);
        log.debug("Outgoing message: verify(attributes:{})", kafkaRecordAttributes);

        /*send message*/
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(buildPackageTopic, "verify", kafkaRecordAttributes));

        boolean success = isSuccess(future);
        producer.close();
        return success;
    }

    private Object throwExceptionOnError(Object data) throws ProjectDataException {
        if (data instanceof Long) {
            throw new ProjectDataException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

}
