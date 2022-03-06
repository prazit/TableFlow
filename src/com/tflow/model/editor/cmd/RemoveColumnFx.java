package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;

import java.util.List;
import java.util.Map;

public class RemoveColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataColumn targetColumn = columnFx.getOwner();
        TransformTable targetTable = (TransformTable) targetColumn.getOwner();

        /*remove remaining lines on startPlug*/
        List<Line> lineList = columnFx.getStartPlug().getLineList();
        if (lineList.size() > 0) {
            for (Line line : lineList) {
                step.removeLine(line);
            }
        }

        /*remove remaining line on endPlug*/
        for (ColumnFxPlug endPlug : columnFx.getEndPlugList()) {
            Line line = endPlug.getLine();
            if (line != null) step.removeLine(line);
        }

        /*remove fx from FxTable*/
        targetTable.getColumnFxTable().getColumnFxList().remove(columnFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*no Action Result*/
    }

}
