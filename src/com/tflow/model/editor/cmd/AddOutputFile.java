package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;

import java.util.List;
import java.util.Map;

/*This command shared between DataTable and TransformTable*/
public class AddOutputFile extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        OutputFile outputFile = (OutputFile) paramMap.get(CommandParamKey.OUTPUT_FILE);
        if (outputFile == null) {
            // for AddOutputFile
            outputFile = new OutputFile(DataFileType.OUT_MD, DataFileType.OUT_MD.getDefaultName(), project.newElementId(), project.newElementId());
            outputFile.setId(project.newUniqueId());
        }/*else{
            // nothing for RemoveOutputFile.Undo
        }*/

        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.add(outputFile);

        step.getSelectableMap().put(outputFile.getSelectableId(), outputFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.OUTPUT_FILE, outputFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.OUTPUT_FILE, outputFile);

        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        if (dataTable instanceof TransformTable) {
            // save OutputFile data
            projectDataManager.addData(ProjectFileType.TRANSFORM_OUTPUT, mapper.map(outputFile), project, outputFile.getId(), step.getId(), 0, dataTable.getId());

            // save OutputFile list
            projectDataManager.addData(ProjectFileType.TRANSFORM_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, outputFile.getId(), step.getId(), 0, dataTable.getId());
        } else {
            // save OutputFile data
            projectDataManager.addData(ProjectFileType.DATA_OUTPUT, mapper.map(outputFile), project, outputFile.getId(), step.getId(), dataTable.getId());

            // save OutputFile list
            projectDataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), project, outputFile.getId(), step.getId(), dataTable.getId());
        }

        // no line, tower, floor to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

}
