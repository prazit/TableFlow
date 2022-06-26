package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;

import java.util.Map;

public class AddDataFile extends Command {
    private static final long serialVersionUID = 2022031309996660005L;

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

            dataFile = new DataFile(null, DataFileType.IN_MD, "Untitled", "/", project.newElementId(), project.newElementId());
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

        // save DataFile data
        ProjectDataManager.addData(ProjectFileType.DATA_FILE, dataFile, project, dataFile.getId(), step.getId());

        // save Line data
        if (newLine != null) {
            ProjectDataManager.addData(ProjectFileType.LINE, newLine, project, newLine.getId(), step.getId());

            // save Line list
            ProjectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, newLine.getId(), step.getId());
        }

        // save Tower data
        ProjectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        ProjectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
    }

}
