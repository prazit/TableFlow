package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.DataTableUtil;

import java.util.List;
import java.util.Map;

/**
 * Extract Data File, Create DataTable and then add to DATA TOWER and DataTable List.
 */
public class AddDataTable extends Command {
    private static final long serialVersionUID = 2022031309996660006L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*support undo of Action 'RemoveDataFile'*/
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        boolean isExecute = dataTable == null;
        if (isExecute) {
            /*execute*/
            dataTable = extractData(dataFile, step);
            dataTable.setLevel(0);
        }

        /*add to Tower*/
        Tower tower = step.getDataTower();
        Floor floor = tower.getAvailableFloor(2, false);
        floor.setRoom(2, dataTable);

        /*Add to selectableMap*/
        DataTableUtil.addTo(step.getSelectableMap(), dataTable, step.getOwner());

        /*Add to DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataTable.setIndex(dataList.size());
        dataList.add(dataTable);

        /*line between DataFile and DataTable*/
        step.addLine(dataFile.getSelectableId(), dataTable.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_TABLE, dataTable);
    }

    private DataTable extractData(DataFile dataFile, Step step) {

        Project project = step.getOwner();

        /*TODO: create compatible Extractor (dataFile.type) | DConvers lib need to make some changes to accept configuration in config class instant*/
        /*TODO: call Extractor.extract*/

        /*-- TODO: remove mockup data below, used to test the command --*/
        DataTable dataTable = new DataTable("Untitled", dataFile, "", project.newElementId(), project.newElementId(), step);

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

        DataTableUtil.generateId(step.getSelectableMap(), dataTable, project);

        return dataTable;
    }

}
