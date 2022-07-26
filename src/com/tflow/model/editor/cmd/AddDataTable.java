package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.*;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Extract Data File, Create DataTable and then add to DATA TOWER and DataTable List.
 */
public class AddDataTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        /*support undo of Action 'RemoveDataFile'*/
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        boolean isExecute = dataTable == null;
        if (isExecute) {
            /*execute*/
            dataTable = extractData(dataFile, step);
            dataTable.setLevel(0);
        }

        /*add to Tower*/
        Tower tower = step.getDataTower();
        EmptyRoom emptyRoom = findEmptyRoom(tower, dataTable);
        Floor floor = tower.getFloor(emptyRoom.getFloorIndex());
        floor.setRoom(emptyRoom.getRoomIndex(), dataTable);

        /*Add to selectableMap*/
        ProjectUtil.addTo(step.getSelectableMap(), dataTable, project);

        /*Add to DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataTable.setIndex(dataList.size());
        dataList.add(dataTable);

        /*line between DataFile and DataTable*/
        Line newLine = addLine(dataFile.getSelectableId(), dataTable.getSelectableId());
        newLine.setId(ProjectUtil.newUniqueId(project));

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_TABLE, dataTable);

        // save DataTable data
        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;
        int dataTableId = dataTable.getId();
        int stepId = step.getId();
        projectDataManager.addData(ProjectFileType.DATA_TABLE, mapper.map(dataTable), project, dataTableId, stepId, dataTableId);

        // save DataTable list
        projectDataManager.addData(ProjectFileType.DATA_TABLE_LIST, mapper.fromDataTableList(dataList), project, dataTableId, stepId);

        // save Object(DataFile) at the endPlug.
        projectDataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), project, dataFile.getId(), stepId);

        // Notice: this command extract columns from Data-File that need to save Column List, Column Data, Output List and Output Data too
        // save Column list
        projectDataManager.addData(ProjectFileType.DATA_COLUMN_LIST, mapper.fromDataColumnList(dataTable.getColumnList()), project, 1, stepId, dataTableId);

        // save Column Data
        for (DataColumn dataColumn : dataTable.getColumnList()) {
            projectDataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), project, dataColumn.getId(), stepId, dataTableId);
        }

        // save Output list
        List<OutputFile> outputList = dataTable.getOutputList();
        projectDataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, 1, stepId, dataTableId);

        // save Output Data
        for (OutputFile outputFile : outputList) {
            projectDataManager.addData(ProjectFileType.DATA_OUTPUT, mapper.map(outputFile), project, outputFile.getId(), stepId, dataTableId);
        }

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, newLine.getId(), stepId);

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, mapper.map(newLine), project, newLine.getId(), stepId);

        // save Tower data
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), stepId);

        // save Step data: need to update Step record every Line added*/
        projectDataManager.addData(ProjectFileType.STEP, mapper.map(step), project, stepId, stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

    /**
     * Find empty room for DataTable only.
     */
    private EmptyRoom findEmptyRoom(Tower dataTower, DataTable dataTable) throws UnsupportedOperationException {
        DataFile dataFile = dataTable.getDataFile();
        int dataFileId = dataFile.getId();
        int dataFileFloorIndex = dataFile.getFloorIndex();
        int dataFileRoomIndex = dataFile.getRoomIndex();
        int dataTableRoomIndex = dataFileRoomIndex + 1;

        Logger log = LoggerFactory.getLogger(AddDataTable.class);
        int floorIndex = dataFileFloorIndex;
        EmptyRoom emptyRoom = null;
        Room room;
        boolean directBrother = false;
        for (; emptyRoom == null; ) {

            /*dataTower always has 3 rooms*/
            room = dataTower.getRoom(floorIndex, dataTableRoomIndex);
            if (room == null) {
                dataTower.addFloor(floorIndex, 1);
                emptyRoom = (EmptyRoom) dataTower.getRoom(floorIndex, dataTableRoomIndex);
                log.warn("foundEmptyRoom({}): at the ground, floorIndex:{}, roomIndex:{}", emptyRoom, floorIndex, dataTableRoomIndex);
                if (!directBrother) moveDataFileTo(dataTower, floorIndex, dataFile);
            } else if (RoomType.EMPTY == room.getRoomType()) {
                emptyRoom = (EmptyRoom) room;
                log.warn("foundEmptyRoom({}): at the same floor, floorIndex:{}, roomIndex:{}", emptyRoom, floorIndex, dataTableRoomIndex);
            } else if (/*not empty and */((DataTable) room).getDataFile().getFloorIndex() <= dataFileFloorIndex) {
                directBrother = ((DataTable) room).getDataFile().getId() == dataFileId;
                floorIndex++;
                log.warn("findEmptyRoom: found brother go next floor, floorIndex:{}, roomIndex:{}", floorIndex, dataTableRoomIndex);
            } else /*not empty and not brothers */ {
                dataTower.addFloor(floorIndex, 1);
                emptyRoom = (EmptyRoom) dataTower.getRoom(floorIndex, dataTableRoomIndex);
                log.warn("foundEmptyRoom({}): under last brother at floorIndex:{}, roomIndex:{}, move dataFile to same floor", emptyRoom, floorIndex, dataTableRoomIndex);
                if (!directBrother) moveDataFileTo(dataTower, floorIndex, dataFile);
            }

        }//end for(;emptyRoom == null)

        return emptyRoom;
    }

    private void moveDataFileTo(Tower dataTower, int floorIndex, DataFile dataFile) throws UnsupportedOperationException {
        int dataFileFloorIndex = dataFile.getFloorIndex();
        int dataFileRoomIndex = dataFile.getRoomIndex();
        Room nextFloorRoom = dataTower.getRoom(floorIndex, dataFileRoomIndex);
        if (RoomType.EMPTY == nextFloorRoom.getRoomType()) {
            dataTower.setEmptyRoom(dataFileFloorIndex, dataFileRoomIndex);
            dataTower.setRoom(floorIndex, dataFileRoomIndex, dataFile);
        } else /*not empty */ {
            DataFile existingDataFile = (DataFile) nextFloorRoom;
            throw new UnsupportedOperationException("moveDataFileTo: floorIndex:" + floorIndex + ", roomIndex:" + dataFileRoomIndex + " is impossible because that room already has DataFile(id:" + existingDataFile.getId() + ", name:" + existingDataFile.getName() + ")");
        }
    }

    private DataTable extractData(DataFile dataFile, Step step) {

        Project project = step.getOwner();

        /*TODO: create compatible Extractor (dataFile.type) | DConvers lib need to make some changes to accept configuration in config class instant*/
        /*TODO: call Extractor.extract*/

        /*-- TODO: remove mockup data below, used to test the command --*/
        DataTable dataTable = new DataTable("Untitled", dataFile, "", ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(4, DataType.DATE, "Date Column", ProjectUtil.newElementId(project), dataTable));

        OutputFile outputCSVFile = new OutputFile(
                DataFileType.OUT_CSV,
                "out/",
                ProjectUtil.newElementId(project),
                ProjectUtil.newElementId(project)
        );

        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.add(outputCSVFile);

        ProjectUtil.generateId(step.getSelectableMap(), dataTable, project);

        return dataTable;
    }

}
