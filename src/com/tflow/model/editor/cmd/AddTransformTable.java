package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.*;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Create TransformTable and copy all column from the source DataTable, add it to the TOWER and TransformTable List.
 */
public class AddTransformTable extends Command {

    private transient Logger log = LoggerFactory.getLogger(AddTransformTable.class);

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable sourceTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Tower tower = step.getTransformTower();
        Project project = step.getOwner();

        SourceType sourceType = getSourceType(sourceTable);

        TransformTable transformTable = new TransformTable("Untitled", sourceTable.getSelectableId(), sourceType, sourceTable.getIdColName(), ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);
        transformTable.setLevel(sourceTable.getLevel() + 1);

        List<DataColumn> sourceColumnList = sourceTable.getColumnList();

        /*copy column from source-table*/
        List<DataColumn> columnList = transformTable.getColumnList();
        for (DataColumn dataColumn : sourceColumnList) {
            columnList.add(new TransformColumn(dataColumn, ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), transformTable));
            /*TODO: in the future need explicitly relation, need to add line between columns as DirectTransferFx*/
        }

        /*need to add ColumnFxTable to the room before Transform Table*/
        EmptyRoom emptyRoom = findEmptyRoom(tower, sourceTable, step.getSelectableMap());
        tower.setRoom(emptyRoom.getFloorIndex(), emptyRoom.getRoomIndex() - 1, transformTable.getColumnFxTable());
        tower.setRoom(emptyRoom.getFloorIndex(), emptyRoom.getRoomIndex(), transformTable);

        ProjectUtil.generateId(step.getSelectableMap(), transformTable, project);
        ProjectUtil.addTo(step.getSelectableMap(), transformTable, project);

        /*Add to TransformTable List*/
        List<TransformTable> transformList = step.getTransformList();
        transformTable.setIndex(transformList.size());
        transformList.add(transformTable);

        /*link from SourceTable to TransformTable*/
        Line newLine = addLine(sourceTable.getSelectableId(), transformTable.getSelectableId());
        newLine.setId(ProjectUtil.newUniqueId(project));

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TRANSFORM_TABLE, transformTable);

        // save TransformTable data including ColumnFxTable
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        int transformTableId = transformTable.getId();
        dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map(transformTable), projectUser, transformTableId, stepId, 0, transformTableId);

        // save TransformTable list
        dataManager.addData(ProjectFileType.TRANSFORM_TABLE_LIST, mapper.fromTransformTableList(transformList), projectUser, 1, stepId);

        // save Object(DataTable) at the endPlug.
        if (sourceTable instanceof TransformTable) {
            dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map((TransformTable) sourceTable), projectUser, sourceTable.getId(), stepId, 0, sourceTable.getId());
        } else {
            dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map(sourceTable), projectUser, sourceTable.getId(), stepId, sourceTable.getId());
        }

        // Notice: this command copy all columns from source table that need to save Column List, Column Data, Output List, Output Data too
        // save Column list
        dataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(transformTable.getColumnList()), projectUser, 1, stepId, 0, transformTableId);

        // save Column Data
        for (DataColumn dataColumn : transformTable.getColumnList()) {
            dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) dataColumn), projectUser, dataColumn.getId(), stepId, 0, transformTableId);
        }

        // save Transformation List
        dataManager.addData(ProjectFileType.TRANSFORMATION_LIST, mapper.fromTableFxList(transformTable.getFxList()), projectUser, 1, stepId, 0, transformTableId);

        // save Transformation Data
        for (TableFx tableFx : transformTable.getFxList()) {
            dataManager.addData(ProjectFileType.TRANSFORMATION, mapper.map(tableFx), projectUser, tableFx.getId(), stepId, 0, transformTableId);
        }

        // save Output List
        List<OutputFile> outputList = transformTable.getOutputList();
        dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT_LIST, mapper.fromOutputFileList(outputList), projectUser, 1, stepId, 0, transformTableId);

        // save Output Data
        for (OutputFile outputFile : outputList) {
            dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT, mapper.map(outputFile), projectUser, outputFile.getId(), stepId, 0, transformTableId);
        }

        // save Line list
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, newLine.getId(), stepId);

        // save Line data
        dataManager.addData(ProjectFileType.LINE, mapper.map(newLine), projectUser, newLine.getId(), stepId);

        // save Tower data
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), stepId);

        // save Step data: need to update Step record every Line added*/
        dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
    }

    private SourceType getSourceType(DataTable sourceTable) {
        if (sourceTable instanceof TransformTable) return SourceType.TRANSFORM_TABLE;
        return SourceType.DATA_TABLE;
    }

    /**
     * Find empty room for TransformTable only.
     */
    private EmptyRoom findEmptyRoom(Tower transformTower, DataTable sourceDataTable, Map<String, Selectable> selectableMap) throws UnsupportedOperationException {
        boolean sameTower = sourceDataTable.getFloor().getTower().getId() == transformTower.getId();
        String sourceDataTableSelectedId = sourceDataTable.getSelectableId();
        int sourceDataTableFloorIndex = sourceDataTable.getFloorIndex();
        int targetRoomIndex = sameTower ? sourceDataTable.getRoomIndex() + 2 : 1;
        int targetFloorIndex = sameTower ? sourceDataTableFloorIndex : 0;

        Logger log = LoggerFactory.getLogger(AddTransformTable.class);
        EmptyRoom emptyRoom = null;
        Room room;
        boolean directBrother = false;
        boolean brotherChecked = false;
        int deadLoopDetection = 100;
        while (emptyRoom == null) {

            if (--deadLoopDetection == 0)
                throw new UnsupportedOperationException("findEmptyRoom: dead loop detected at floorIndex:" + targetFloorIndex + ", roomIndex:" + targetRoomIndex);

            /*transformTower has unlimited rooms*/
            room = transformTower.getRoom(targetFloorIndex, targetRoomIndex);
            if (room == null) {
                if (directBrother || !sameTower || (!directBrother && sameTower && brotherChecked)) transformTower.addFloor(targetFloorIndex, 1);
                else transformTower.addRoom(2);
                emptyRoom = (EmptyRoom) transformTower.getRoom(targetFloorIndex, targetRoomIndex);
                log.warn("foundEmptyRoom({}): at the ground, targetFloorIndex:{}, roomIndex:{}", emptyRoom, targetFloorIndex, targetRoomIndex);
                continue;
            } else if (RoomType.EMPTY == room.getRoomType()) {
                emptyRoom = (EmptyRoom) room;
                log.warn("foundEmptyRoom({}): at the same floor, targetFloorIndex:{}, roomIndex:{}", emptyRoom, targetFloorIndex, targetRoomIndex);
                continue;
            }

            String sourceSelectableId = ((TransformTable) room).getSourceSelectableId();
            DataTable sourceTable = (DataTable) selectableMap.get(sourceSelectableId);
            if (/*not empty and */sourceTable.getFloorIndex() <= sourceDataTableFloorIndex) {
                directBrother = sourceSelectableId.compareTo(sourceDataTableSelectedId) == 0;
                brotherChecked = true;
                targetFloorIndex++;
                log.warn("findEmptyRoom: found brother go next floor, targetFloorIndex:{}, roomIndex:{}", targetFloorIndex, targetRoomIndex);
            } else /*not empty and not brothers */ {
                if (directBrother || !sameTower || (!directBrother && sameTower && brotherChecked)) transformTower.addFloor(targetFloorIndex, 1);
                else transformTower.addRoom(2);
                emptyRoom = (EmptyRoom) transformTower.getRoom(targetFloorIndex, targetRoomIndex);
                log.warn("foundEmptyRoom({}): under last brother at targetFloorIndex:{}, roomIndex:{}, move dataFile to same floor", emptyRoom, targetFloorIndex, targetRoomIndex);
            }

        }//end for(;emptyRoom == null)

        return emptyRoom;
    }

}
