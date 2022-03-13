package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.datasource.Local;

import java.util.ArrayList;
import java.util.Map;

public class AddOutputFile extends Command {
    private static final long serialVersionUID = 2022031309996660010L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        DataFile dataFile = new DataFile(null, DataFileType.OUT_MD, "Untitled", "/", project.newElementId(), project.newElementId());
        dataFile.setId(project.newUniqueId());

        dataTable.getOutputList().add(dataFile);

        step.getSelectableMap().put(dataFile.getSelectableId(), dataFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);
    }

}
