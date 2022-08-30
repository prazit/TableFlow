package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

public class RemoveDirectLine extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line line = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        removeLine(line);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, line);

        /*Action Result*/
        /*nothing*/

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_REMOVED, line);

        // save Line data
        Project project = step.getOwner();
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.LINE, (TWData) null, projectUser, line.getId(), step.getId());

        // save Line list
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, line.getId(), step.getId());

        // no tower, floor to save here
    }

}
