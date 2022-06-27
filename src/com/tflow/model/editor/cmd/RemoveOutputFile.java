package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.List;
import java.util.Map;

public class RemoveOutputFile extends Command {
    private static final long serialVersionUID = 2022031309996660010L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        DataTable dataTable = (DataTable) dataFile.getOwner();
        List<DataFile> outputList = dataTable.getOutputList();
        outputList.remove(dataFile);

        step.getSelectableMap().remove(dataFile.getSelectableId(), dataFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);

        // save OutputFile data
        ProjectDataManager.addData(ProjectFileType.DATA_OUTPUT, null, project, dataFile.getId(), step.getId(), dataTable.getId());

        // save OutputFile list
        ProjectDataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, outputList, project, dataFile.getId(), step.getId(), dataTable.getId());

        // no line, tower, floor to save here
    }

}
