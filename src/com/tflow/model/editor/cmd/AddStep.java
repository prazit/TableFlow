package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.mapper.ProjectMapper;

import java.util.List;
import java.util.Map;

public class AddStep extends Command {
    private static final long serialVersionUID = 2022031309996660009L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        int stepId = project.newUniqueId();
        step.setId(stepId);

        List<Step> stepList = project.getStepList();
        int stepIndex = stepList.size();
        step.setIndex(stepIndex);

        stepList.add(step);

        // save Step data
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.STEP, mapper.map(step), project, stepId, stepId);

        // save Step List
        projectDataManager.addData(ProjectFileType.STEP_LIST, mapper.fromStepList(stepList), project, stepId, stepId);

        // no line, tower, floor to save here
    }
}
