package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.util.ProjectUtil;

import java.util.List;
import java.util.Map;

public class ExtractSystemEnvironment extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        Project project = step.getOwner();

        /*-- TODO: remove mockup data below, used to test the command --*/
        DataTable dataTable = new DataTable("Untitled", dataFile, "", ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal Column", ProjectUtil.newElementId(project), dataTable));
        columnList.add(new DataColumn(4, DataType.DATE, "Date Column", ProjectUtil.newElementId(project), dataTable));

        OutputFile outputCSVFile = new OutputFile(
                DataFileType.OUT_CSV,
                "out/",
                ProjectUtil.newElementId(project),
                ProjectUtil.newElementId(project)
        );

        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.add(outputCSVFile);

        ProjectUtil.generateId(step.getSelectableMap(), dataTable, project);

        /*Return result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.DATA_TABLE, dataTable);
    }
}
