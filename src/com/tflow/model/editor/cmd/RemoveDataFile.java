package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

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
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, ProjectUtil.newElementId(project)));

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
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.DATA_FILE_LIST, fileList, projectUser, 0, stepId);

        // save DataFile data
        dataManager.addData(ProjectFileType.DATA_FILE, (TWData) null, projectUser, dataFile.getId(), stepId);

        // save Line data
        for (Line line : removedLineList) {
            dataManager.addData(ProjectFileType.LINE, (TWData) null, projectUser, line.getId(), stepId);
        }

        // save Line list
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, 1, stepId);

        // save Tower data
        Tower tower = floor.getTower();
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), stepId);

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
