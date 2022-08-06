package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

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
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);
    }
}
