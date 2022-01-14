package com.tflow.model.editor.cmd;

import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;

import java.util.List;
import java.util.Map;

public class AddStep extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        step.setId(project.newUniqueId());

        List<Step> stepList = project.getStepList();
        int stepIndex = stepList.size();
        step.setIndex(stepIndex);

        stepList.add(step);
    }
}
