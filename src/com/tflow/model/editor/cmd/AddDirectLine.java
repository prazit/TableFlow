package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

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
        newLine.setId(ProjectUtil.newUniqueId(project));
        newLine.setUser(true);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, newLine);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(newLine);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_ADDED, newLine);

        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);

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
        dataManager.addData(ProjectFileType.LINE, mapper.map(newLine), projectUser, newLine.getId(), step.getId());

        // save Line list
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, newLine.getId(), step.getId());

        // no tower, floor to save here

        // save Step data: need to update Step record every Line added*/
        dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, step.getId(), step.getId());

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
    }

}
