package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        Map<String, Object> propertyMap;
        DataColumn sourceColumn;
        TransformColumn targetColumn;
        ColumnFunction columnFunction;

        /*support undo command of Action 'RemoveColumnFx'*/
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        boolean isExecute = (columnFx == null);
        if (isExecute) {
            /*Action 'AddColumnFx'.execute*/
            targetColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
            sourceColumn = (DataColumn) paramMap.get(CommandParamKey.DATA_COLUMN);
            columnFunction = (ColumnFunction) paramMap.get(CommandParamKey.COLUMN_FUNCTION);
            columnFx = new ColumnFx(columnFunction, columnFunction.getName(), project.newElementId(), (DataColumn) targetColumn);
            columnFx.setId(project.newUniqueId());
            propertyMap = columnFx.getPropertyMap();
            initPropertyMap(propertyMap, sourceColumn);
        } else {
            /*Action 'RemoveColumnFx'.executeUndo*/
            propertyMap = columnFx.getPropertyMap();
            targetColumn = (TransformColumn) columnFx.getOwner();
            String sourceSelectableId = (String) propertyMap.get("sourceColumn");
            sourceColumn = (DataColumn) selectableMap.get(sourceSelectableId);
            columnFunction = columnFx.getFunction();
        }

        TransformTable transformTable = (TransformTable) targetColumn.getOwner();
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.add(columnFx);
        columnFxList.sort(Comparator.comparingInt(columnFx2 -> columnFx2.getOwner().getIndex()));

        selectableMap.put(columnFx.getSelectableId(), columnFx);

        List<Line> lineList = new ArrayList<>();
        if (isExecute) {
            /*Notice: draw lines below tested on ColumnFunction.LOOKUP and expect to work for all ColumnFunction*/

            /*line between sourceColumn and columnFx*/
            Line line1 = step.addLine(sourceColumn.getSelectableId(), columnFx.getSelectableId());

            /*line between columnFx and targetColumn*/
            Line line2 = step.addLine(columnFx.getSelectableId(), targetColumn.getSelectableId());

            lineList.add(line1);
            lineList.add(line2);
        }

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*Action Result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.LINE_LIST, lineList);
        resultMap.put(ActionResultKey.COLUMN_FX, columnFx);
    }

    private void initPropertyMap(Map<String, Object> propertyMap, DataColumn sourceColumn) {
        propertyMap.put("sourceTable", sourceColumn.getOwner().getSelectableId());
        propertyMap.put("sourceColumn", sourceColumn.getSelectableId());
    }

}
