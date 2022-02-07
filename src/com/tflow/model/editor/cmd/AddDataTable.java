package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

/**
 * Extract Data File, Create DataTable and then add to DATA TOWER and DataTable List.
 */
public class AddDataTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        DataTable dataTable = extractData(dataFile, project);

        dataTable.setId(step.getOwner().newUniqueId());
        assignChildId(dataTable, step.getOwner());

        Tower tower = step.getDataTower();

        Floor floor = tower.getAvailableFloor(2, false);
        floor.setRoom(2, dataTable);

        updateSelectableMap(step.getSelectableMap(), dataTable);

        step.addLine(dataFile.getSelectableId(), dataTable.getSelectableId());

        /*Add to DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataTable.setIndex(dataList.size());
        dataList.add(dataTable);

        /*Action Result*/
        action.getResultMap().put("dataTable", dataTable);
    }

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

    private DataTable extractData(DataFile dataFile, Project project) {

        /*TODO: create compatible Extractor (dataFile.type) | DConvers lib need to make some changes to accept configuration in config class instant*/
        /*TODO: call Extractor.extract*/

        /*-- TODO: remove mockup data below, used to test the command --*/
        DataTable dataTable = new DataTable("Untitled", dataFile, "", project.newElementId(), project.newElementId());

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(4, DataType.DATE, "Date Column", project.newElementId(), dataTable));

        Local myComputer = new Local("MyComputer", "C:/myData/", project.newElementId());
        DataFile outputCSVFile = new DataFile(
                myComputer,
                DataFileType.OUT_CSV,
                "output.csv",
                "out/",
                project.newElementId(),
                project.newElementId()
        );

        List<DataFile> outputList = dataTable.getOutputList();
        outputList.add(outputCSVFile);

        return dataTable;
    }

}
