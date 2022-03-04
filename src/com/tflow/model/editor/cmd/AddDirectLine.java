package com.tflow.model.editor.cmd;

import com.tflow.model.editor.Line;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddDirectLine extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line line = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        line = step.addLine(line.getStartSelectableId(), line.getEndSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, line);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(line);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);
    }

}
