package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;

import java.util.ArrayList;
import java.util.List;
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

        /*remove remaining lines on startPlug (startPlug connect to many DataTable)*/
        LinePlug startPlug = dataFile.getStartPlug();
        List<Line> removedLineList = new ArrayList<>(startPlug.getLineList());
        step.removeLine(startPlug);

        /*remove remaining line on endPlug (endPlug connect to one DataSource)*/
        LinePlug endPlug = dataFile.getEndPlug();
        removedLineList.add(endPlug.getLine());
        step.removeLine(endPlug.getLine());

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

        // save DataFile data
        ProjectDataManager projectDataManager = project.getManager();
        projectDataManager.addData(ProjectFileType.DATA_FILE, null, project, dataFile.getId(), step.getId());

        // save Line data
        for (Line line : removedLineList) {
            projectDataManager.addData(ProjectFileType.LINE, null, project, line.getId(), step.getId());
        }

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, 1, step.getId());

        // save Tower data
        Tower tower = floor.getTower();
        projectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
    }

}
