package com.tflow.model.editor;

import com.google.gson.internal.LinkedTreeMap;
import com.tflow.kafka.*;
import com.tflow.model.data.*;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.*;

public class ProjectManager {

    public ProjectManager() {
        /*nothing*/
    }

    public void saveProjectAs(String newProjectId, Project project) {
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

        String oldId = project.getId();
        project.setId(newProjectId);
        ProjectData projectData = mapper.map(project);
        ProjectUser projectUser = mapper.toProjectUser(project);

        dataManager.addData(ProjectFileType.PROJECT, projectData, projectUser, newProjectId);

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
        ProjectDataManager dataManager = project.getDataManager();
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

            /*add each transform-columnfx in transform-table(columnFxTable)*/
            for (ColumnFx columnFx : transformTable.getColumnFxTable().getColumnFxList()) {
                dataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, mapper.map(columnFx), projectUser, columnFx.getId(), stepId, 0, transformTableId);
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

    /**
     * TODO: need to support open new project from template (projectId < 0)
     **/
    @SuppressWarnings("unchecked")
    public Project loadProject(Workspace workspace, ProjectDataManager dataManager) throws ClassCastException, ProjectDataException {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

        String projectId = workspace.getProject().getId();
        long clientId = workspace.getClient().getId();
        long userId = workspace.getUser().getId();
        
        /*get project, to know the project is not edit by another */
        Object data = dataManager.getData(ProjectFileType.PROJECT, new KafkaRecordAttributes(clientId, userId, projectId, projectId));
        Project project = mapper.map((ProjectData) throwExceptionOnError(data));
        project.setOwner(workspace);
        project.setDataManager(dataManager);

        /*get db-list*/
        ProjectUser projectUser = mapper.toProjectUser(project);
        data = dataManager.getData(ProjectFileType.DB_LIST, projectUser, "1");
        List<Integer> databaseIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, Database> databaseMap = new HashMap<>();
        project.setDatabaseMap(databaseMap);

        /*get each db in db-list*/
        for (Integer id : databaseIdList) {
            data = dataManager.getData(ProjectFileType.DB, projectUser, String.valueOf(id));
            databaseMap.put(id, mapper.map((DatabaseData) throwExceptionOnError(data)));
        }

        /*get sftp-list*/
        data = dataManager.getData(ProjectFileType.SFTP_LIST, projectUser, "2");
        List<Integer> sftpIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, SFTP> sftpMap = new HashMap<>();
        project.setSftpMap(sftpMap);

        /*get each sftp in sftp-list*/
        for (Integer id : sftpIdList) {
            data = dataManager.getData(ProjectFileType.SFTP, projectUser, String.valueOf(id));
            sftpMap.put(id, mapper.map((SFTPData) throwExceptionOnError(data)));
        }

        /*get local-list*/
        data = dataManager.getData(ProjectFileType.LOCAL_LIST, projectUser, "3");
        List<Integer> localIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
        Map<Integer, Local> localMap = new HashMap<>();
        project.setLocalMap(localMap);

        /*get each local in local-list*/
        for (Integer id : localIdList) {
            data = dataManager.getData(ProjectFileType.LOCAL, projectUser, String.valueOf(id));
            localMap.put(id, mapper.map((LocalData) throwExceptionOnError(data)));
        }

        /*get variable-list*/
        data = dataManager.getData(ProjectFileType.VARIABLE_LIST, projectUser, "4");
        List<String> varIdList = (List<String>) throwExceptionOnError(data);
        Map<String, Variable> varMap = new HashMap<>();
        project.setVariableMap(varMap);

        /*get each variable in variable-list*/
        for (String id : varIdList) {
            data = dataManager.getData(ProjectFileType.VARIABLE, projectUser, id);
            varMap.put(id, mapper.map((VariableData) throwExceptionOnError(data)));
        }

        /*get step-list*/
        data = dataManager.getData(ProjectFileType.STEP_LIST, projectUser, "5");
        List<StepItemData> stepItemDataList = mapper.fromLinkedTreeMap((List<LinkedTreeMap>) throwExceptionOnError(data));
        project.setStepList(mapper.toStepList(stepItemDataList));

        workspace.setProject(project);
        return project;
    }

    @SuppressWarnings("unchecked")
    public Step loadStep(Project project, int stepIndex) throws Exception {
        ProjectDataManager dataManager = project.getDataManager();
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

            /*create each floor in tower*/
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
        step.setDataTower(towerList.get(0));
        step.setTransformTower(towerList.get(1));
        step.setOutputTower(towerList.get(2));

        /*get data-source-selector-list*/
        data = dataManager.getData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, projectUser, 1, stepId);
        List<Integer> dataSourceSelectorIdList = mapper.fromDoubleList(((List<Double>) throwExceptionOnError(data)));
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
        List<Integer> dataFileIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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
        List<Integer> dataTableIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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
            List<Integer> columnIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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
            List<Integer> outputIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<OutputFile> outputList = new ArrayList<>();
            dataTable.setOutputList(outputList);

            /*get each output in output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = dataManager.getData(ProjectFileType.DATA_OUTPUT, projectUser, outputId, stepId, dataTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(dataTable);
                outputList.add(outputFile);
            }

        }// end of for:DataTableIdList

        /*get transform-table-list*/
        data = dataManager.getData(ProjectFileType.TRANSFORM_TABLE_LIST, projectUser, 9, stepId);
        List<Integer> transformTableIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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

            ColumnFxTable columnFxTable = transformTable.getColumnFxTable();
            columnFxTable.setFloorIndex(transformTable.getFloorIndex());
            columnFxTable.setRoomIndex(transformTable.getRoomIndex() - 1);
            step.getTransformTower().setRoom(columnFxTable.getFloorIndex(), columnFxTable.getRoomIndex(), columnFxTable);

            /*get transform-column-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> columnIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<DataColumn> columnList = new ArrayList<>();
            transformTable.setColumnList(columnList);

            /*get each transform-column in transform-column-list*/
            List<ColumnFx> columnFxList = columnFxTable.getColumnFxList();
            TransformColumn transformColumn;
            ColumnFx columnFx;
            for (Integer columnId : columnIdList) {
                data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMN, projectUser, columnId, stepId, 0, transformTableId);
                transformColumn = mapper.map((TransformColumnData) throwExceptionOnError(data));
                transformColumn.setOwner(transformTable);
                columnList.add(transformColumn);
                transformColumn.createPlugListeners();

                /*get each transform-columnfx in transform-table(columnFxTable)*/
                ColumnFx fx = transformColumn.getFx();
                if (fx != null) {
                    data = dataManager.getData(ProjectFileType.TRANSFORM_COLUMNFX, projectUser, fx.getId(), stepId, 0, transformTableId);
                    columnFx = mapper.map((ColumnFxData) throwExceptionOnError(data));
                    columnFx.setOwner(transformColumn);
                    transformColumn.setFx(columnFx);
                    columnFxList.add(columnFx);
                    for (ColumnFxPlug columnFxPlug : columnFx.getEndPlugList()) {
                        columnFxPlug.setOwner(columnFx);
                    }
                    columnFx.createPlugListeners();
                }
            }

            /*get transform-output-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> outputIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
            List<OutputFile> outputList = new ArrayList<>();
            transformTable.setOutputList(outputList);

            /*get each transform-output in transform-output-list*/
            OutputFile outputFile;
            for (Integer outputId : outputIdList) {
                data = dataManager.getData(ProjectFileType.TRANSFORM_OUTPUT, projectUser, outputId, stepId, 0, transformTableId);
                outputFile = mapper.map((OutputFileData) throwExceptionOnError(data));
                outputFile.setOwner(transformTable);
                outputList.add(outputFile);
            }

            /*get tranformation-list*/
            data = dataManager.getData(ProjectFileType.TRANSFORMATION_LIST, projectUser, 1, stepId, 0, transformTableId);
            List<Integer> fxIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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
        List<Integer> lineIdList = mapper.fromDoubleList((List<Double>) throwExceptionOnError(data));
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

    private Object throwExceptionOnError(Object data) throws ProjectDataException {
        if (data instanceof Long) {
            throw new ProjectDataException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }


}
