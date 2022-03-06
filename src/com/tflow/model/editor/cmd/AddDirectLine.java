package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

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

        /*------- step.addLine -------*/

        newLine.setClientIndex(step.newLineClientIndex());
        step.getLineList().add(newLine);

        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Selectable startSelectable = selectableMap.get(newLine.getStartSelectableId());
        Selectable endSelectable = selectableMap.get(newLine.getEndSelectableId());

        newLine.setType(getLineType(startSelectable));

        LinePlug startPlug = startSelectable.getStartPlug();
        startPlug.setPlugged(true);
        startPlug.getLineList().add(newLine);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setStartPlug(startPlug);

        HasEndPlug hasEndPlug = (HasEndPlug) endSelectable;
        LinePlug endPlug = hasEndPlug.getEndPlug();
        endPlug.setPlugged(true);
        endPlug.getLineList().add(newLine);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setEndPlug(endPlug);

        /*------- step.addLine -------*/

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.LINE, newLine);

        /*Action Result*/
        List<Line> lineList = new ArrayList<>();
        lineList.add(newLine);
        action.getResultMap().put(ActionResultKey.LINE_LIST, lineList);

        /*notify status*/
        step.getEventManager().fireEvent(EventName.LINE_ADDED, newLine);
    }

    private LineType getLineType(Selectable selectable) {
        if (selectable instanceof DataColumn) {
            DataColumn dataColumn = (DataColumn) selectable;
            return LineType.valueOf(dataColumn.getType().name());
        } else if (selectable instanceof ColumnFx) {
            ColumnFx columnFx = (ColumnFx) selectable;
            return LineType.valueOf(columnFx.getOwner().getType().name());
        } else {
            return LineType.TABLE;
        }
    }

}
