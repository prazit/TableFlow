package com.tflow.util;

import com.tflow.model.editor.*;

import java.util.Map;

public class DataTableUtil {

    /**
     * Add all child of DataTable (and TransformTable) to selectable-map.
     * Generate Id for all child of DataTable (and TransformTable).
     *
     * @param selectableMap target map.
     * @param dataTable     look for child in this table.
     * @param project       used to generate unique id.
     */
    public static void renewChild(Map<String, Selectable> selectableMap, DataTable dataTable, Project project) {
        dataTable.setId(project.newUniqueId());
        selectableMap.put(dataTable.getSelectableId(), dataTable);

        for (DataColumn column : dataTable.getColumnList()) {
            column.setOwner(dataTable);
            column.setId(project.newUniqueId());
            selectableMap.put(column.getSelectableId(), column);
        }

        for (DataFile output : dataTable.getOutputList()) {
            output.setOwner(dataTable);
            output.setId(project.newUniqueId());
            selectableMap.put(output.getSelectableId(), output);
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx == null) continue;
            fx.setId(project.newUniqueId());
            fx.setOwner(column);
            selectableMap.put(column.getSelectableId(), fx);
        }

        for (TableFx fx : transformTable.getFxList()) {
            fx.setId(project.newUniqueId());
            fx.setOwner(transformTable);
            selectableMap.put(fx.getSelectableId(), fx);
        }
    }

}
