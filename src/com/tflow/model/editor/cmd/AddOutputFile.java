package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.datasource.Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddOutputFile extends Command {
    private static final long serialVersionUID = 2022031309996660010L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        if(dataFile == null) {
            // for AddOutputFile
            dataFile = new DataFile(null, DataFileType.OUT_MD, "Untitled", "/", project.newElementId(), project.newElementId());
            dataFile.setId(project.newUniqueId());
        }/*else{
            // nothing for RemoveOutputFile.Undo
        }*/

        List<DataFile> outputList = dataTable.getOutputList();
        outputList.add(dataFile);

        step.getSelectableMap().put(dataFile.getSelectableId(), dataFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);

        // save OutputFile data
        ProjectDataManager projectDataManager = project.getManager();
        projectDataManager.addData(ProjectFileType.DATA_OUTPUT, dataFile, project, dataFile.getId(), step.getId(), dataTable.getId());

        // save OutputFile list
        projectDataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, outputList, project, dataFile.getId(), step.getId(), dataTable.getId());

        // no line, tower, floor to save here
    }

}
