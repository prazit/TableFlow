package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.StepList;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

public class SelectStep extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        int stepIndex = (Integer) paramMap.get(CommandParamKey.INDEX);

        /*move stepIndex into the list*/
        StepList<Step> stepList = project.getStepList();
        int size = stepList.size();
        if (stepIndex >= size) {
            stepIndex = size - 1;
        }

        int oldStepIndex = project.getActiveStepIndex();
        boolean changed = oldStepIndex != stepIndex;
        if (changed) project.setActiveStepIndex(stepIndex);

        // for Action.executeUndo
        paramMap.put(CommandParamKey.INDEX, oldStepIndex);

        // result map

        // save Project data
        if (changed) {
            DataManager dataManager = project.getDataManager();
            ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
            ProjectUser projectUser = mapper.toProjectUser(project);
            dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

            // need to wait commit thread after addData.
            dataManager.waitAllTasks();
        }

    }

}
