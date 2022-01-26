package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        int id = project.newUniqueId();
        columnFx.setId(id);

        TransformTable transformTable = (TransformTable) columnFx.getOwner().getOwner();
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.add(columnFx);

        Selectable selectable = (Selectable) columnFx;
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        selectableMap.put(selectable.getSelectableId(), selectable);

        /*draw lines, this is a case of lookup for example only, TODO: need to find solution to support all ColumnFunction by itself*/
        if (ColumnFunction.LOOKUP == columnFx.getFunction()) {
            /*line between sourceTable and columnFx*/
            String columnSelectableId = (String) columnFx.getPropertyMap().get("sourceColumn");
            DataColumn sourceColumn = (DataColumn) selectableMap.get(columnSelectableId);
            DataTable sourceTable = sourceColumn.getOwner();
            step.addLine(sourceTable.getSelectableId(), columnFx.getSelectableId());

            /*line between columnFx and tranformTable*/
            step.addLine(columnFx.getSelectableId(), transformTable.getSelectableId());
        }
    }
}
