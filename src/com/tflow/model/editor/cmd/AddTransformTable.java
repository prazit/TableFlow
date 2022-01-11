package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Add TransformTable to TOWER and TransformTable List.
 */
public class AddTransformTable extends Command {

    private Logger log = LoggerFactory.getLogger(AddTransformTable.class);

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Tower tower = step.getTransformTower();
        Project project = step.getOwner();

        transformTable.setId(project.newUniqueId());

        DataTable sourceTable;
        SourceType sourceType = transformTable.getSourceType();
        int sourceId = transformTable.getSourceId();
        List<DataColumn> sourceColumnList;
        Room sourceRoom;
        switch (sourceType) {
            case DATA_TABLE:
                sourceTable = step.getDataTable(sourceId);
                break;

            case TRANSFORM_TABLE:
                sourceTable = step.getTransformTable(sourceId);
                break;

            default: /*case MAPPING_TABLE:*/
                throw new UnsupportedOperationException("Unsupported source type " + sourceType + "(Id:" + sourceId + ") used by Transform Table(\" + transformTable.getName() + \")");
        }
        sourceRoom = (Room) sourceTable;
        sourceColumnList = sourceTable.getColumnList();

        /*copy column from source-table*/
        List<DataColumn> columnList = transformTable.getColumnList();
        for (DataColumn dataColumn : sourceColumnList) {
            columnList.add(new TransformColumn(dataColumn, project.newElementId(), project.newElementId(), transformTable));
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
            if (floor.getRoomList().get(1).equals(sourceRoom)) {
                /*case: floor already used by source table then add 2 rooms to the right*/
                tower.addRoom(2, null);
                roomIndex += 2;
            } else {
                /*case: floor already used by another table then add new floor to the next*/
                floor = tower.getAvailableFloor(-1, true, ++floorIndex);
            }
        }
        assert floor != null;
        floor.setRoom(roomIndex, transformTable);

        step.getSelectableMap().put(transformTable.getSelectableId(), transformTable);

        /*link from SourceTable to TransformTable*/
        step.addLine(sourceTable.getSelectableId(), transformTable.getSelectableId());

    }

}
