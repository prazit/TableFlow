package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;

import java.util.List;
import java.util.Map;

public class RemoveOutputFile extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        OutputFile outputFile = (OutputFile) paramMap.get(CommandParamKey.OUTPUT_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        DataTable dataTable = (DataTable) outputFile.getOwner();
        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.remove(outputFile);

        step.getSelectableMap().remove(outputFile.getSelectableId(), outputFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.OUTPUT_FILE, outputFile);

        // save OutputFile data
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.DATA_OUTPUT, (TWData) null, project, outputFile.getId(), step.getId(), dataTable.getId());

        // save OutputFile list
        projectDataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, 0, step.getId(), dataTable.getId());

        // no line, tower, floor to save here
    }

}
