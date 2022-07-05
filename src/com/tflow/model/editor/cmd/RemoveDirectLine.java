package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;

import java.util.Map;

public class RemoveDirectLine extends Command {
    private static final long serialVersionUID = 2022031309996660015L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line line = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        step.removeLine(line);
        
        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, line);

        /*Action Result*/
        /*nothing*/

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_REMOVED, line);

        // save Line data
        Project project = step.getOwner();
        ProjectDataManager projectDataManager = project.getManager();
        projectDataManager.addData(ProjectFileType.LINE, null, project, line.getId(), step.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, line.getId(), step.getId());

        // no tower, floor to save here
    }

}
