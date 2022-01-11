package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

/**
 * Add DataTable to TOWER and DataTable List.
 */
public class AddDataTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        dataTable.setId(step.getOwner().newUniqueId());
        assignChildId(dataTable, step.getOwner());

        Tower tower = step.getDataTower();

        Floor floor = tower.getAvailableFloor(2, false);
        floor.setRoom(2, dataTable);

        updateSelectableMap(step.getSelectableMap(), dataTable);

        DataFile dataFile = dataTable.getDataFile();
        step.addLine(dataFile.getSelectableId(), dataTable.getSelectableId());

        /*Add to DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataTable.setIndex(dataList.size());
        dataList.add(dataTable);

    }

    /**
     * Mockup Data and Template only.
     */
    private void assignChildId(DataTable dataTable, Project project) {

        for (DataColumn column : dataTable.getColumnList()) {
            column.setId(project.newUniqueId());
        }

        for (DataFile output : dataTable.getOutputList()) {
            output.setId(project.newUniqueId());
        }

        if (!(dataTable instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) dataTable;

        for (DataColumn column : dataTable.getColumnList()) {
            ColumnFx fx = ((TransformColumn) column).getFx();
            if (fx != null) fx.setId(project.newUniqueId());
        }

        for (TableFx tableFx : transformTable.getFxList()) {
            tableFx.setId(project.newUniqueId());
        }

    }

    private void updateSelectableMap(Map<String, Selectable> selectableMap, DataTable dataTable) {
        selectableMap.put(dataTable.getSelectableId(), dataTable);

        for (DataColumn column : dataTable.getColumnList()) {
            selectableMap.put(column.getSelectableId(), column);
        }

        for (DataFile output : dataTable.getOutputList()) {
            selectableMap.put(output.getSelectableId(), output);
        }

        if (dataTable instanceof TransformTable) {
            TransformTable tt = (TransformTable) dataTable;

            for (DataColumn column : tt.getColumnList()) {
                ColumnFx fx = ((TransformColumn) column).getFx();
                if (fx != null) selectableMap.put(column.getSelectableId(), fx);
            }

            for (TableFx fx : tt.getFxList()) {
                selectableMap.put(fx.getSelectableId(), fx);
            }
        }
    }

}
