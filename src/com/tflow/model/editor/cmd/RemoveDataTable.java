package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.util.DataTableUtil;

import java.util.Map;

/**
 * Remove DataTable only.<br/>
 * Notice: the DataTable need to remove all related objects before.
 */
public class RemoveDataTable extends Command {
    private static final long serialVersionUID = 2022031309996660011L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*TODO: need to create actions to remove all child at first, history will change between this process (keep no chain no effect)*/

        /*remove line between dataFile and dataTable*/
        step.removeLine(dataTable.getEndPlug());

        /*remove from Tower*/
        Floor floor = dataTable.getFloor();
        int roomIndex = dataTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, step.getOwner().newElementId()));

        /*remove from DataTable List*/
        step.getDataList().remove(dataTable);

        /*remove from selectableMap*/
        DataTableUtil.removeFrom(step.getSelectableMap(), dataTable);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.DATA_FILE, dataTable.getDataFile());
    }

}
