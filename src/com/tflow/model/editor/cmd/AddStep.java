package com.tflow.model.editor.cmd;

import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;

import java.io.IOException;
import java.rmi.server.ServerNotActiveException;
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
        ProjectDataManager.addData(ProjectFileType.STEP, step, project, stepId, stepId);

        // no line, tower, floor to save here
    }
}
