package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.EventName;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddDirectLine extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line newLine = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        newLine = step.addLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
        newLine.setId(project.newUniqueId());
        newLine.setUser(true);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, newLine);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(newLine);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_ADDED, newLine);

        // save Line data
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.LINE, mapper.map(newLine), project, newLine.getId(), step.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, newLine.getId(), step.getId());

        // no tower, floor to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

}
