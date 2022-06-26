package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.DataTableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Create TransformTable and copy all column from the source DataTable, add it to the TOWER and TransformTable List.
 */
public class AddTransformTable extends Command {
    private static final long serialVersionUID = 2022031309996660002L;

    private transient Logger log = LoggerFactory.getLogger(AddTransformTable.class);

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable sourceTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Tower tower = step.getTransformTower();
        Project project = step.getOwner();

        SourceType sourceType = getSourceType(sourceTable);

        TransformTable transformTable = new TransformTable("Untitled", sourceTable.getSelectableId(), sourceType, sourceTable.getIdColName(), project.newElementId(), project.newElementId(), step);
        transformTable.setLevel(sourceTable.getLevel() + 1);

        Room sourceRoom = (Room) sourceTable;
        List<DataColumn> sourceColumnList = sourceTable.getColumnList();

        /*copy column from source-table*/
        List<DataColumn> columnList = transformTable.getColumnList();
        for (DataColumn dataColumn : sourceColumnList) {
            columnList.add(new TransformColumn(dataColumn, project.newElementId(), project.newElementId(), transformTable));
            /*TODO: in the future need explicitly relation, need to add line between columns as DirectTransferFx*/
        }

        /*put this transform-table on the same floor of source table*/
        int floorIndex = sourceRoom.getFloor().getIndex();
        Floor floor = tower.getFloor(floorIndex);
        int roomIndex = 1;
        if (floor == null) {
            /*case:Tower need more floor.*/
            for (int fi = tower.getFloorList().size(); fi <= floorIndex; fi++) {
                floor = tower.getAvailableFloor(-1, true);
            }
        } else if (!floor.isEmpty()) {
            List<Room> roomList = floor.getRoomList();
            if (roomList.get(roomList.size() - 1).equals(sourceRoom)) {
                /*case: floor already used by source table then add 2 rooms to the right*/
                tower.addRoom(2, null);
                roomIndex = roomList.size() - 1;
            } else {
                /*case: floor already used by another table then add new floor to the next*/
                floor = tower.getAvailableFloor(-1, true, ++floorIndex);
            }
        }
        assert floor != null;

        /*need to add ColumnFxTable to the room before Transform Table*/
        floor.setRoom(roomIndex - 1, transformTable.getColumnFxTable());
        floor.setRoom(roomIndex, transformTable);

        DataTableUtil.generateId(step.getSelectableMap(), transformTable, project);
        DataTableUtil.addTo(step.getSelectableMap(), transformTable, project);

        /*link from SourceTable to TransformTable*/
        Line newLine = step.addLine(sourceTable.getSelectableId(), transformTable.getSelectableId());
        newLine.setId(project.newUniqueId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TRANSFORM_TABLE, transformTable);

        // save TransformTable data including ColumnFxTable
        ProjectDataManager.addData(ProjectFileType.TRANSFORM_TABLE, transformTable, step.getOwner(), transformTable.getId(), step.getId(), 0, transformTable.getId());

        // save TransformTable list
        //ProjectDataManager.addData(ProjectFileType.DATA_TABLE_LIST, transformTableList, step.getOwner(), dataFile.getId(), step.getId());

        // save Line data
        ProjectDataManager.addData(ProjectFileType.LINE, newLine, project, newLine.getId(), step.getId());

        // save Line list
        ProjectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, newLine.getId(), step.getId());

        // save Tower data
        ProjectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data (both TransformTable and ColumnFxTable)
        ProjectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
    }

    private SourceType getSourceType(DataTable sourceTable) {
        if (sourceTable instanceof TransformTable) return SourceType.TRANSFORM_TABLE;
        return SourceType.DATA_TABLE;
    }

}
