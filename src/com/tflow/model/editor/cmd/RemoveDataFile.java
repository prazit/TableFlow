package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.DataTableUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Remove DataFile only.<br/>
 * Notice: the DataFile need to remove all related objects before.
 */
public class RemoveDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        List<DataFile> fileList = step.getFileList();
        fileList.remove(dataFile);

        /*remove remaining lines on startPlug (startPlug connect to many DataTable)*/
        LinePlug startPlug = dataFile.getStartPlug();
        List<Line> removedLineList = new ArrayList<>(startPlug.getLineList());
        removeLine(startPlug);

        /*remove remaining line on endPlug (endPlug connect to one DataSource)*/
        LinePlug endPlug = dataFile.getEndPlug();
        removedLineList.add(endPlug.getLine());
        removeLine(endPlug.getLine());

        /*remove from Tower*/
        Floor floor = dataFile.getFloor();
        int roomIndex = dataFile.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, DataTableUtil.newElementId(project)));

        /*remove from DataTable*/
        HasDataFile owner = dataFile.getOwner();
        if (owner != null) {
            owner.setDataFile(null);
        }

        /*remove from selectableMap*/
        step.getSelectableMap().remove(dataFile.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        // save DataFile list
        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;
        int stepId = step.getId();
        projectDataManager.addData(ProjectFileType.DATA_FILE_LIST, fileList, project, 0, stepId);

        // save DataFile data
        projectDataManager.addData(ProjectFileType.DATA_FILE, (TWData) null, project, dataFile.getId(), stepId);

        // save Line data
        for (Line line : removedLineList) {
            projectDataManager.addData(ProjectFileType.LINE, (TWData) null, project, line.getId(), stepId);
        }

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, 1, stepId);

        // save Tower data
        Tower tower = floor.getTower();
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), stepId);
    }

}
