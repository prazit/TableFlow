package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;

import java.util.Map;

public class RemoveDirectLine extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Line line = (Line) paramMap.get(CommandParamKey.LINE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        /*------- step.removeLine -------*/
        //step.removeLine(line);
        step.getLineList().remove(line);

        LinePlug startPlug = line.getStartPlug();
        startPlug.getLineList().remove(line);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }

        LinePlug endPlug = line.getEndPlug();
        endPlug.getLineList().remove(line);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }

        /*------- step.removeLine -------*/

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, line);

        /*Action Result*/
        /*nothing*/

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_REMOVED, line);
    }

}
