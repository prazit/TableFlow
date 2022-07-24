package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;

import java.util.Map;

public class SelectObject extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Selectable selectable = (Selectable) paramMap.get(CommandParamKey.SELECTABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        Selectable oldSelected = step.getActiveObject();
        step.setActiveObject(selectable);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.SELECTABLE, oldSelected);

        /*Action Result*/

        // save Step Data for ActiveObject
        Project project = step.getOwner();
        ProjectDataManager dataManager = project.getDataManager();
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.STEP, dataManager.mapper.map(step), project, stepId, stepId);
    }
}
