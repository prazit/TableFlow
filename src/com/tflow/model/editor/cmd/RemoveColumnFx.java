package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;

import java.util.Map;

public class RemoveColumnFx extends Command {
    private static final long serialVersionUID = 2022031309996660008L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataColumn targetColumn = columnFx.getOwner();
        TransformTable targetTable = (TransformTable) targetColumn.getOwner();

        /*remove remaining lines on startPlug*/
        step.removeLine(columnFx.getStartPlug());

        /*remove remaining line on endPlug*/
        for (ColumnFxPlug endPlug : columnFx.getEndPlugList()) {
            step.removeLine(endPlug);
        }

        /*remove fx from FxTable*/
        targetTable.getColumnFxTable().getColumnFxList().remove(columnFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*no Action Result*/
    }

}
