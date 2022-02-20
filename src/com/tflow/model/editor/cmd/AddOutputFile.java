package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.Local;

import java.util.List;
import java.util.Map;

public class AddOutputFile extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        /*TODO: may be need to detect existing local and reuse them*/
        Local local = new Local("Untitled", "/", project.newElementId());

        DataFile dataFile = new DataFile(local, DataFileType.OUT_MD, "Untitled", "/", project.newElementId(), project.newElementId());
        dataTable.getOutputList().add(dataFile);

        dataFile.setId(project.newUniqueId());

        step.getSelectableMap().put(dataFile.getSelectableId(), dataFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put("dataFile", dataFile);
    }

}
