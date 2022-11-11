package com.tflow.model.editor.cmd;

import com.clevel.dconvers.data.DataRow;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.util.DConversHelper;
import com.tflow.util.ProjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class ExtractCommand extends Command {

    protected Logger log;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        log = LoggerFactory.getLogger(getClass());

        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        /*create source table configs*/
        DConversHelper dConversHelper = new DConversHelper();
        initProperties(dConversHelper, dataFile, step, project);
        if (log.isDebugEnabled()) dConversHelper.printProperties(log);

        /*start DConvers to create source table by configs above*/
        if (!dConversHelper.run()) {
            Throwable firstError = dConversHelper.getFirstError();
            throw new UnsupportedOperationException((firstError == null ? "DConvers exit with some error, exit code = " + dConversHelper.getExitCode() + "." : firstError.getMessage()));
        }

        /*got extracted table, get source table by id*/
        String dConversTableId = dConversHelper.getString("source");
        String idColName = dConversHelper.getString("source." + dConversTableId + ".id");

        com.clevel.dconvers.data.DataTable extractedTable = dConversHelper.getSourceTable(dConversTableId);

        DataRow firstRow = extractedTable.getRow(0);
        if (firstRow == null) {
            throw new UnsupportedOperationException("No data received from the SQL! please add at lease one row before extract data.");
        }

        if ((idColName == null || idColName.isEmpty()) && firstRow != null) {
            idColName = firstRow.getColumn(0).getName();
        }
        log.debug("idColName = '{}'", idColName);

        /*create data-table*/
        String name = dataFile.getName().split("[.]")[0];
        DataTable dataTable = new DataTable(name, dataFile, idColName, ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);

        /*copy column from extracted-table*/
        if (firstRow != null) {
            List<DataColumn> columnList = dataTable.getColumnList();
            Map<String, String> nameMap = new HashMap<>();
            int computeColumnIndex = 0;
            String columnName;
            String temp;
            int maximumNameLength = 40; //TODO: need configs for column.name.max.length
            for (com.clevel.dconvers.data.DataColumn extractedColumn : firstRow.getColumnList()) {
                columnName = extractedColumn.getName();
                if (columnName.length() > maximumNameLength) {
                    if (columnName.replaceFirst("[\\+\\-\\*\\/\\(\\)\\|\\&]", "").length() < columnName.length()) {
                        columnName = "COMPUTED" + (++computeColumnIndex);
                    } else {
                        columnName = columnName.substring(0, maximumNameLength);
                        temp = columnName;
                        int count = 0;
                        while (nameMap.get(temp) != null) {
                            temp = columnName + (++count);
                        }
                        columnName = temp;
                        if (extractedColumn.getName().equalsIgnoreCase(idColName)) {
                            dataTable.setIdColName(columnName);
                        }
                    }
                }
                nameMap.put(columnName, columnName);

                columnList.add(new DataColumn(extractedColumn.getIndex(), DataType.parse(extractedColumn.getType()), columnName, ProjectUtil.newElementId(project), dataTable));
            }
        }

        /*Add to selectableMap*/
        ProjectUtil.generateId(step.getSelectableMap(), dataTable, project);
        ProjectUtil.addTo(step.getSelectableMap(), dataTable, project);

        /*Return result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.DATA_TABLE, dataTable);
    }

    protected abstract void initProperties(DConversHelper dConversHelper, DataFile dataFile, Step step, Project project);
}
