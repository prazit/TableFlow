package com.tflow.model.editor.cmd;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.system.Properties;
import com.tflow.util.DConversHelper;
import com.tflow.util.ProjectUtil;

import java.util.List;

public abstract class QueryCommand extends Command {

    protected void loadColumnList(QueryTable queryTable, DataFile dataFile, Project project, Workspace workspace) {
        String tableName = queryTable.getName().toUpperCase();
        String tableSchema = queryTable.getSchema();
        /*log.debug("loadColumnList: table: {}={}", tableName, queryTable);*/

        /*load table list from Database using DConvers*/
        int dataSourceId = dataFile.getDataSourceId();
        Database database = project.getDatabaseMap().get(dataSourceId);
        String shortenDBMS = database.getDbms().name().split("[_]")[0].toLowerCase();
        Properties configs = workspace.getConfigs("dconvers." + shortenDBMS + ".");
        tableSchema = tableSchema == null ? database.getSchema().toUpperCase() : tableSchema;

        /*load SYNONYMS info by table_name*/
        DConversHelper dConvers = new DConversHelper();
        String dbSchema = tableSchema;
        String dbTable = tableName;
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);
        String synonymsTableName = "synonyms";
        dConvers.addSourceTable(synonymsTableName, 1, dataSourceName, configs.getProperty("sql.synonyms"), "TABLE_NAME");
        dConvers.addVariable("table", tableName.toUpperCase());
        dConvers.addConsoleOutput(synonymsTableName);
        if (dConvers.run()) {
            DataTable synonymsTable = dConvers.getSourceTable(synonymsTableName);
            if (synonymsTable != null) {
                DataRow row = synonymsTable.getRow(0);
                dbTable = row.getColumn(0).getValue();
                dbSchema = row.getColumn(1).getValue();
            }
        } else {
            /*log.warn("load SYNONYMS failed but allow to try next step.");*/
        }

        /*load column list by table_name and owner*/
        dConvers = new DConversHelper();
        dataSourceName = dConvers.addDatabase(dataSourceId, project);
        String columnsTableName = "columns";
        dConvers.addSourceTable(columnsTableName, 1, dataSourceName, configs.getProperty("sql.columns"), "");
        dConvers.addVariable("schema", dbSchema);
        dConvers.addVariable("table", dbTable);
        dConvers.addConsoleOutput(columnsTableName);
        if (!dConvers.run()) {
            throw new UnsupportedOperationException("Load Column List Failed by DConvers!");
        }

        /*if (log.isDebugEnabled()) {
            byte[] configFileContent = dConvers.getConfigFile();
            log.debug("DConvers Config File: {}", configFileContent == null ? "null" : new String(configFileContent, StandardCharsets.ISO_8859_1));
        }*/

        List<QueryColumn> columnList = queryTable.getColumnList();
        DataTable tables = dConvers.getSourceTable(columnsTableName);
        if (tables.getRowList().size() == 0) {
            throw new UnsupportedOperationException("No column for table " + tableName + "!");
        }

        /*first column must be column-name*/
        QueryColumn queryColumn;
        DataColumn column;
        String columnName;
        int index = 0;
        for (DataRow row : tables.getRowList()) {
            column = row.getColumn(0);
            columnName = column.getValue();
            queryColumn = new QueryColumn(index++, ProjectUtil.newUniqueId(project), columnName, queryTable);
            queryColumn.setValue(tableName + "." + columnName);
            columnList.add(queryColumn);
        }
    }


}
