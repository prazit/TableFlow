package com.tflow.model.editor.cmd;

import com.tflow.model.editor.EventName;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddDirectLine extends Command {
    private static final long serialVersionUID = 2022031309996660016L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line newLine = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        newLine = step.addLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
        newLine.setUser(true);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, newLine);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(newLine);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_ADDED, newLine);
    }

}
