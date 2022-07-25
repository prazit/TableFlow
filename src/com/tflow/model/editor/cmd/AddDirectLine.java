package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.DataTableUtil;

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

        newLine = addLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
        newLine.setId(DataTableUtil.newUniqueId(project));
        newLine.setUser(true);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, newLine);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(newLine);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_ADDED, newLine);

        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;

        // save Object at startPlug.
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Selectable selectable = selectableMap.get(newLine.getStartSelectableId());
        if (!saveSelectableData(selectable, step)) {
            throw new UnsupportedOperationException("Save data of Unsupported Type " + selectable.getClass().getName() + "(" + selectable.getSelectableId() + ")");
        }

        // save Object at endPlug.
        if (!saveSelectableData(selectableMap.get(newLine.getEndSelectableId()), step)) {
            throw new UnsupportedOperationException("Save data of Unsupported Type " + selectable.getClass().getName() + "(" + selectable.getSelectableId() + ")");
        }

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, mapper.map(newLine), project, newLine.getId(), step.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, newLine.getId(), step.getId());

        // no tower, floor to save here

        // save Step data: need to update Step record every Line added*/
        projectDataManager.addData(ProjectFileType.STEP, mapper.map(step), project, step.getId(), step.getId());

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

}
