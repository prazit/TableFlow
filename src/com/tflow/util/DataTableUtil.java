package com.tflow.util;

import com.tflow.model.editor.*;

import java.util.Map;

public class DataTableUtil {

    /**
     * Add all child of DataTable (and TransformTable) to selectable-map.
     *
     * @param selectableMap target map.
     * @param dataTable     look for child in this table.
     * @param project       used to generate unique id.
     */
    public static void addTo(Map<String, Selectable> selectableMap, DataTable dataTable, Project project) {
        selectableMap.put(dataTable.getSelectableId(), dataTable);

        for (DataColumn column : dataTable.getColumnList()) {
            selectableMap.put(column.getSelectableId(), column);
        }

        for (DataFile output : dataTable.getOutputList()) {
            selectableMap.put(output.getSelectableId(), output);
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx == null) continue;
            selectableMap.put(column.getSelectableId(), fx);
        }

        for (TableFx fx : transformTable.getFxList()) {
            selectableMap.put(fx.getSelectableId(), fx);
        }
    }

    public static void removeFrom(Map<String, Selectable> selectableMap, DataTable dataTable) {
        selectableMap.remove(dataTable.getSelectableId());

        for (DataColumn column : dataTable.getColumnList()) {
            selectableMap.remove(column.getSelectableId());
        }

        for (DataFile output : dataTable.getOutputList()) {
            selectableMap.remove(output.getSelectableId());
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx == null) continue;
            selectableMap.remove(column.getSelectableId());
        }

        for (TableFx fx : transformTable.getFxList()) {
            selectableMap.remove(fx.getSelectableId());
        }
    }

    /**
     * Generate Id for all child of DataTable (and TransformTable).
     *
     * @param selectableMap target map.
     * @param dataTable     look for child in this table.
     * @param project       used to generate unique id.
     */
    public static void generateId(Map<String, Selectable> selectableMap, DataTable dataTable, Project project) {
        dataTable.setId(project.newUniqueId());

        for (DataColumn column : dataTable.getColumnList()) {
            column.setOwner(dataTable);
            column.setId(project.newUniqueId());
        }

        for (DataFile output : dataTable.getOutputList()) {
            output.setOwner(dataTable);
            output.setId(project.newUniqueId());
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx == null) continue;
            fx.setId(project.newUniqueId());
            fx.setOwner(column);
        }

        for (TableFx fx : transformTable.getFxList()) {
            fx.setId(project.newUniqueId());
            fx.setOwner(transformTable);
        }
    }
}
