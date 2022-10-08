package com.tflow.model.editor.cmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.util.ProjectUtil;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
        DConvers dConvers = new DConvers(new String[]{
                "--library-mode=preset",
                "--logback=" + getClass().getResource("logback.xml"),
                "--verbose"
        });

        Configuration properties = dConvers.dataConversionConfigFile.getProperties();
        initProperties(properties, dConvers, dataFile, step, project);
        if (log.isDebugEnabled()) printProperties(properties);

        /*start DConvers to create source table by configs above*/
        try {
            dConvers.start();
        } catch (Exception ex) {
            log.debug("dConvers.dataConversionConfigFile={}", dConvers.dataConversionConfigFile);
            throw new UnsupportedOperationException("Unexpected exception from DConvers: ", ex);
        }

        /*got extracted table, get source table by id*/
        String dConversTableId = properties.getString("source");
        String idColName = properties.getString("source." + dConversTableId + ".id", null);

        Converter converter = dConvers.converterList.get(0);
        com.clevel.dconvers.data.DataTable extractedTable = converter.getDataTable("SRC:" + dConversTableId);

        DataRow firstRow = extractedTable.getRow(0);
        if (idColName == null && firstRow != null) {
            idColName = firstRow.getColumn(0).getName();
        }
        log.debug("idColName = '{}'", idColName);

        /*create data-table*/
        String name = dataFile.getName().split("[.]")[0];
        DataTable dataTable = new DataTable(name, dataFile, idColName, ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);

        /*copy column from extracted-table*/
        if (firstRow != null) {
            List<DataColumn> columnList = dataTable.getColumnList();
            for (com.clevel.dconvers.data.DataColumn extractedColumn : firstRow.getColumnList()) {
                columnList.add(new DataColumn(extractedColumn.getIndex(), DataType.parse(extractedColumn.getType()), extractedColumn.getName(), ProjectUtil.newElementId(project), dataTable));
            }
        }

        /*Add to selectableMap*/
        ProjectUtil.generateId(step.getSelectableMap(), dataTable, project);
        ProjectUtil.addTo(step.getSelectableMap(), dataTable, project);

        /*Return result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.DATA_TABLE, dataTable);
    }

    protected void addTableProperties(Configuration properties, String tableName, int tableIndex, String datasource, String query, String idColumnName) {
        properties.addProperty("source", tableName);
        String dConversSourceKey = "source." + tableName;
        properties.addProperty(dConversSourceKey + ".index", String.valueOf(tableIndex));
        properties.addProperty(dConversSourceKey + ".datasource", datasource);
        properties.addProperty(dConversSourceKey + ".query", query);
        properties.addProperty(dConversSourceKey + ".id", idColumnName);
    }

    protected void addOutputProperties(Configuration properties, String dConversTableId) {
        String prefix = "source." + dConversTableId + ".markdown";
        properties.addProperty(prefix, "true");
        properties.addProperty(prefix + ".output", "console");
        properties.addProperty(prefix + ".mermaid", "false");
        properties.addProperty(prefix + ".comment", "false");
        properties.addProperty(prefix + ".comment.datasource", "true");
        properties.addProperty(prefix + ".comment.query", "true");
        properties.addProperty(prefix + ".title", "false");
    }

    private void printProperties(Configuration properties) {
        Iterator<String> keyList = properties.getKeys();
        StringBuilder msg = new StringBuilder();
        while (keyList.hasNext()) {
            String key = keyList.next();
            if (key.equals("source")) {
                for (Object source : properties.getList(key)) {
                    msg.append(",'source':'").append(source).append("'");
                }
            } else {
                msg.append(",'").append(key).append("':'").append(properties.getString(key)).append("'");
            }
        }
        msg.setCharAt(0, '{');
        msg.append("}");
        log.debug("DConvers-Properties: {}", msg.toString());
    }

    protected abstract void initProperties(Configuration properties, DConvers dConvers, DataFile dataFile, Step step, Project project);
}
