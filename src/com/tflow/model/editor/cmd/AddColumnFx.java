package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataColumn sourceColumn = (DataColumn) paramMap.get(CommandParamKey.DATA_COLUMN);
        TransformColumn targetColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
        ColumnFunction columnFunction = (ColumnFunction) paramMap.get(CommandParamKey.COLUMN_FUNCTION);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        /*optional JAVASCRIPT_BUILDER*/
        StringBuilder jsBuilder = null;
        Object obj = paramMap.get(CommandParamKey.JAVASCRIPT_BUILDER);
        if (obj != null) {
            jsBuilder = (StringBuilder) obj;
        }

        ColumnFx columnFx = new ColumnFx((DataColumn) targetColumn, columnFunction, columnFunction.getName(), project.newElementId(), project.newElementId());

        Map<String, Object> propertyMap = columnFx.getPropertyMap();
        propertyMap.put("sourceTable", sourceColumn.getOwner().getSelectableId());
        propertyMap.put("sourceColumn", sourceColumn.getSelectableId());

        columnFx.setId(project.newUniqueId());

        TransformTable transformTable = (TransformTable) targetColumn.getOwner();
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.add(columnFx);
        columnFxList.sort(Comparator.comparingInt(columnFx2 -> columnFx2.getOwner().getIndex()));

        step.getSelectableMap().put(columnFx.getSelectableId(), columnFx);

        /*Notice: draw lines below tested on ColumnFunction.LOOKUP and expect to work for all ColumnFunction*/

        /*line between sourceColumn and columnFx*/
        Line line1 = step.addLine(sourceColumn.getSelectableId(), columnFx.getSelectableId());

        /*line between columnFx and targetColumn*/
        Line line2 = step.addLine(columnFx.getSelectableId(), targetColumn.getSelectableId());

        if (jsBuilder != null) {
            jsBuilder.append(line1.getJsAdd());
            jsBuilder.append(line2.getJsAdd());
        }

        /*Action Result*/
        action.getResultMap().put("columnFx", columnFx);
    }
}
