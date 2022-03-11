package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;

import java.util.ArrayList;
import java.util.List;
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
        if (dataTable != null) {
            step.addLine(selectableId, dataTable.getSelectableId());
        }

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);
    }

}
