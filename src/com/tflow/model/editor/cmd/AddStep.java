package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.List;
import java.util.Map;

public class AddStep extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        List<Step> stepList = project.getStepList();

        Step step = new Step("Untitled", project);
        step.setIndex(stepList.size());
        stepList.add(step);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.STEP, step);

        /*Action Result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.STEP, step);

        // save Step data
        project.getManager().saveStep(step, project);
    }
}
