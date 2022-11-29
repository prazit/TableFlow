package com.tflow.model.editor.cmd;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.query.ColumnType;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.DataType;
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

    protected void loadTableName(QueryTable queryTable, DataFile dataFile, Project project, Workspace workspace) {
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
        String dbSynonym = tableName;
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
                dbSynonym = row.getColumn(2).getValue();
            }
        }

        queryTable.setSchema(dbSchema);
        queryTable.setName(dbTable);
        queryTable.setAlias(dbSynonym);
    }

    protected void loadColumnList(QueryTable queryTable, DataFile dataFile, Project project, Workspace workspace) {
        String tableName = queryTable.getName();
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
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);

        /*load column list by table_name and owner*/
        dConvers = new DConversHelper();
        dataSourceName = dConvers.addDatabase(dataSourceId, project);
        String columnsTableName = "columns";
        dConvers.addSourceTable(columnsTableName, 1, dataSourceName, configs.getProperty("sql.columns"), "");
        dConvers.addVariable("schema", tableSchema);
        dConvers.addVariable("table", tableName);
        dConvers.addConsoleOutput(columnsTableName);
        if (!dConvers.run()) {
            throw new UnsupportedOperationException("Load Column List Failed by DConvers!");
        }

        List<QueryColumn> columnList = queryTable.getColumnList();
        DataTable tables = dConvers.getSourceTable(columnsTableName);
        if (tables.getRowList().size() == 0) {
            throw new UnsupportedOperationException("No column for table " + tableName + "!");
        }

        /*IMPORTANT: Notice: column order must be following
          0 column-name
          1 data-type
          2 constraint-type ( 'P'=primary-key, 'R'=foreign-key )
          3 foreign-table-schema
          4 foreign-table-name
         */
        QueryColumn queryColumn;
        DataColumn column;
        String columnName;
        String consType;
        String tableAlias = queryTable.getAlias();
        int index = 0;
        for (DataRow row : tables.getRowList()) {
            column = row.getColumn(0);
            columnName = column.getValue();
            queryColumn = new QueryColumn(index++, ProjectUtil.newUniqueId(project), columnName, queryTable);
            queryColumn.setValue(tableAlias + "." + columnName);
            consType = row.getColumn(2).getValue();
            queryColumn.setDataType(getColumnType(row.getColumn(1).getValue(), database.getDbms()));
            queryColumn.setPk(consType.equals("P"));
            queryColumn.setFk(consType.equals("R"));
            if (queryColumn.isFk()) {
                queryColumn.setFkSchema(row.getColumn(3).getValue());
                queryColumn.setFkTable(row.getColumn(4).getValue());
            }
            columnList.add(queryColumn);
        }
    }

    /*TODO: move this function to DBMSFunctions class for Oracle*/
    private DataType getColumnType(String type, Dbms dbms) {
        /*dbms == ORACLE*/
        type = type.trim().toUpperCase();
        if (type.equals("DATE")) return DataType.DATE;
        if (type.equals("NUMBER")) return DataType.DECIMAL;
        return DataType.STRING;
    }

    protected void markSelectedColumn(QueryTable queryTable, List<QueryColumn> selectedColumnList) {
        String tableName = queryTable.getName().toUpperCase();
        String tableAlias = queryTable.getAlias().toUpperCase();
        for (QueryColumn selected : selectedColumnList) {
            if (selected.getType() != ColumnType.COMPUTE) {
                String[] tableColumn = selected.getValue().toUpperCase().split("[.]");
                if (tableName.equals(tableColumn[0]) || tableAlias.equals(tableColumn[0])) {
                    selected.setOwner(queryTable);
                    QueryColumn column = findColumn(tableColumn[1], queryTable);
                    if (column != null) {
                        selected.setSelected(true);
                        selected.setId(column.getId());
                        selected.setOwner(column.getOwner());
                        column.setSelected(true);
                    }
                }/* else {
                    log.debug("markSelectedColumn: ignore different table({}) and selected-table({}), selected-column: {}", tableName, tableColumn[0], selected);
                }*/
            }
        }
    }

    private QueryColumn findColumn(String columnName, QueryTable queryTable) {
        for (QueryColumn column : queryTable.getColumnList()) {
            if (columnName.equals(column.getName().toUpperCase())) {
                return column;
            }
        }
        /*log.debug("findColumn: column({}) not found on table({})", columnName, queryTable.getName());*/
        // need null instead of throw new UnsupportedOperationException("Invalid Column Reference: '" + columnName + "' not found in table '" + queryTable.getName() + "'");
        return null;
    }


}
