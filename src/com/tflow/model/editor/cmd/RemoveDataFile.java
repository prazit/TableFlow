package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;

import java.util.Map;

/**
 * Remove DataFile only.<br/>
 * Notice: the DataFile need to remove all related objects before.
 */
public class RemoveDataFile extends Command {
    private static final long serialVersionUID = 2022031309996660012L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*remove remaining lines on startPlug*/
        step.removeLine(dataFile.getStartPlug());

        /*remove remaining line on endPlug*/
        step.removeLine(dataFile.getEndPlug());

        /*remove from Tower*/
        Floor floor = dataFile.getFloor();
        int roomIndex = dataFile.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, project.newElementId()));

        /*remove from DataTable*/
        HasDataFile owner = dataFile.getOwner();
        if (owner != null) {
            owner.setDataFile(null);
        }

        /*remove from selectableMap*/
        step.getSelectableMap().remove(dataFile.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);
    }

}
