package com.tflow.util;

import com.tflow.model.editor.*;

import java.util.Map;

public class DataTableUtil {

    /**
     * Generate new element id (unique within the project)
     *
     * @return String elementId
     */
    public static String newElementId(Project project) {
        int lastElementId = project.getLastElementId() + 1;
        project.setLastElementId(lastElementId);
        return "em" + lastElementId;
    }

    /**
     * Generate new id (unique within the project)
     *
     * @return int id
     */
    public static int newUniqueId(Project project) {
        int lastUniqueId = project.getLastUniqueId() + 1;
        project.setLastUniqueId(lastUniqueId);
        return lastUniqueId;
    }

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
        dataTable.setId(newUniqueId(project));

        for (DataColumn column : dataTable.getColumnList()) {
            column.setOwner(dataTable);
            column.setId(newUniqueId(project));
        }

        for (OutputFile output : dataTable.getOutputList()) {
            output.setOwner(dataTable);
            output.setId(newUniqueId(project));
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx == null) continue;
            fx.setId(newUniqueId(project));
            fx.setOwner(column);
        }

        for (TableFx fx : transformTable.getFxList()) {
            fx.setId(newUniqueId(project));
            fx.setOwner(transformTable);
        }
    }
}
