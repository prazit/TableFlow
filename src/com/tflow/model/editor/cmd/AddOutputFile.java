package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

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
            outputFile = new OutputFile(DataFileType.OUT_MD, DataFileType.OUT_MD.getDefaultName(), ProjectUtil.newElementId(project), ProjectUtil.newElementId(project));
            outputFile.setId(ProjectUtil.newUniqueId(project));
        }/*else{
            // nothing for RemoveOutputFile.Undo
        }*/

        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.add(outputFile);
        outputFile.setOwner(dataTable);
        outputFile.createOwnerEventHandlers();

        step.getSelectableMap().put(outputFile.getSelectableId(), outputFile);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.OUTPUT_FILE, outputFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.OUTPUT_FILE, outputFile);

        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        if (dataTable instanceof TransformTable) {
            // save OutputFile data
            dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT, mapper.map(outputFile), projectUser, outputFile.getId(), step.getId(), 0, dataTable.getId());

            // save OutputFile list
            dataManager.addData(ProjectFileType.TRANSFORM_OUTPUT_LIST, mapper.fromOutputFileList(outputList), projectUser, outputFile.getId(), step.getId(), 0, dataTable.getId());
        } else {
            // save OutputFile data
            dataManager.addData(ProjectFileType.DATA_OUTPUT, mapper.map(outputFile), projectUser, outputFile.getId(), step.getId(), dataTable.getId());

            // save OutputFile list
            dataManager.addData(ProjectFileType.DATA_OUTPUT_LIST, mapper.fromOutputFileList(outputList), projectUser, outputFile.getId(), step.getId(), dataTable.getId());
        }

        // no line, tower, floor to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
