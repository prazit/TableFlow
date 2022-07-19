package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;

import java.util.Map;

public class AddDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Tower tower = step.getDataTower();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*support undo of Action 'RemoveDataFile'*/
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        DataTable dataTable;
        if (dataFile == null) {
            /*execute*/
            dataTable = null;

            dataFile = new DataFile(DataFileType.IN_MD, "/", project.newElementId(), project.newElementId());
            dataFile.setId(project.newUniqueId());

        } else {
            /*executeUndo*/
            dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
            dataFile.setOwner(dataTable);
        }

        Floor floor = tower.getAvailableFloor(1, false);
        floor.setRoom(1, dataFile);

        String selectableId = dataFile.getSelectableId();
        step.getSelectableMap().put(selectableId, dataFile);

        /*line between dataFile and dataTable*/
        Line newLine = null;
        if (dataTable != null) {
            newLine = step.addLine(selectableId, dataTable.getSelectableId());
            newLine.setId(project.newUniqueId());
        }

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);

        // save DataFile list
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        int stepId = step.getId();
        projectDataManager.addData(ProjectFileType.DATA_FILE_LIST, mapper.fromDataFileList(step.getFileList()), project, 0, stepId);

        // save DataFile data
        projectDataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), project, dataFile.getId(), stepId);

        if (newLine != null) {
            // save Line data
            projectDataManager.addData(ProjectFileType.LINE, mapper.map(newLine), project, newLine.getId(), stepId);

            // save Line list
            projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, newLine.getId(), stepId);

            // no object at the startPlug to save here
        }

        // save Tower data
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), stepId);

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, mapper.map(floor), project, floor.getId(), stepId);
    }

}
