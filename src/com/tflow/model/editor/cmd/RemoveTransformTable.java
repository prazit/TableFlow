package com.tflow.model.editor.cmd;

import com.tflow.model.editor.DataTable;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.TransformTable;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;

import java.util.Map;

/**
 * Remove TransformTable only.<br/>
 * Notice: the TransformTable need to remove all related objects before.
 */
public class RemoveTransformTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*remove from Tower*/
        Floor floor = transformTable.getFloor();
        int roomIndex = transformTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, step.getOwner().newElementId()));

        /*remove from TransformTable List*/
        step.getTransformList().remove(transformTable);

        /*remove from selectableMap*/
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        selectableMap.remove(transformTable.getSelectableId());

        /*for Action.executeUndo()*/
        DataTable dataTable = (DataTable) selectableMap.get(transformTable.getSourceSelectableId());
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
    }

}