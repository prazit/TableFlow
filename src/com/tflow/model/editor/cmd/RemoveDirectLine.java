package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;

import java.util.Map;

public class RemoveDirectLine extends Command {

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
    }

}
