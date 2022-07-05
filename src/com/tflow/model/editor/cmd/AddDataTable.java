package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
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
        Project project = step.getOwner();
        Line newLine = step.addLine(dataFile.getSelectableId(), dataTable.getSelectableId());
        newLine.setId(project.newUniqueId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_TABLE, dataTable);

        // save DataTable data
        ProjectDataManager projectDataManager = project.getManager();
        int dataTableId = dataTable.getId();
        projectDataManager.addData(ProjectFileType.DATA_TABLE, dataTable, step.getOwner(), dataTableId, step.getId(), dataTableId);

        // save DataTable list
        projectDataManager.addData(ProjectFileType.DATA_TABLE_LIST, dataList, step.getOwner(), dataTableId, step.getId());

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, newLine, project, newLine.getId(), step.getId());

        // save Object(DataFile) at the endPlug.
        projectDataManager.addData(ProjectFileType.DATA_FILE, dataFile, step.getOwner(), dataFile.getId(), step.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, newLine.getId(), step.getId());

        // save Tower data
        projectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
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
