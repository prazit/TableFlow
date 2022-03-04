package com.tflow.model.editor.cmd;

import com.tflow.model.editor.Line;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveDirectLine extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line line = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        step.removeLine(line);

        /*for Action.executeUndo()*/
        // paramMap.put(CommandParamKey.LINE, line);
    }

}
