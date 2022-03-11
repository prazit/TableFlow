package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;

import java.util.List;
import java.util.Map;

/**
 * Remove DataFile only.<br/>
 * Notice: the DataTable need to remove all related objects before.
 */
public class RemoveDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*remove remaining lines on startPlug*/
        List<Line> lineList = dataFile.getStartPlug().getLineList();
        if (lineList.size() > 0) {
            for (Line line : lineList) {
                step.removeLine(line);
            }
        }

        /*remove remaining line on endPlug*/
        lineList = dataFile.getEndPlug().getLineList();
        if (lineList.size() > 0) {
            for (Line line : lineList) {
                step.removeLine(line);
            }
        }

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
