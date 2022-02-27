package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.DataTableUtil;

import java.util.List;
import java.util.Map;

/**
 * Remove DataTable only.<br/>
 * Notice: the DataTable need to remove all related objects before.
 */
public class RemoveDataTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*remove from Tower*/
        Floor floor = dataTable.getFloor();
        int roomIndex = dataTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, step.getOwner().newElementId()));

        /*remove from DataTable List*/
        step.getDataList().remove(dataTable);

        /*remove from selectableMap*/
        step.getSelectableMap().remove(dataTable.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.DATA_FILE, dataTable.getDataFile());
    }

}
