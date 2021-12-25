package com.tflow.model.editor.cmd;

import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {

        /** TODO: link to/from columnFx.
         for (TransformColumn transformColumn : transformTable.getColumnList()) {
         LineType lineType = LineType.valueOf(transformColumn.getType().name());
         ColumnFx fx = transformColumn.getFx();
         if (fx == null) continue;

         /*Link from SourceTable to ColumnFx* /
         lineList.add(new Line(sourceDataColumn.getStartPlug(), fx.getEndPlug(), lineType));

         /*link from ColumnFx to TransformTable* /
         lineList.add(new Line(fx.getStartPlug(), transformColumn.getEndPlug(), lineType));
         }
         */

    }
}
