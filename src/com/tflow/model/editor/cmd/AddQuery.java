package com.tflow.model.editor.cmd;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.data.query.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.sql.*;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.system.Properties;
import com.tflow.util.DConversHelper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AddQuery extends Command {

    private Workspace workspace;
    private Project project;
    private DataFile dataFile;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Logger log = LoggerFactory.getLogger(getClass());

        workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        project = workspace.getProject();
        dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        BinaryFile sqlFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);

        Query query = new Query();
        query.setId(ProjectUtil.newUniqueId(project));
        dataFile.getPropertyMap().put(PropertyVar.queryId.name(), query.getId());

        /* assume sql is simple select (no nested) */
        String sql = new String(sqlFile.getContent(), StandardCharsets.ISO_8859_1).replaceAll("[\\s]+", " ");
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();
        splitSQLPart(sql, select, from, where, orderBy);
        log.debug("AddQuery: select = {}", select);
        log.debug("AddQuery: from = {}", from);
        log.debug("AddQuery: where = {}", where);
        log.debug("AddQuery: orderBy = {}", where);

        /*select => columnList*/
        String[] selectArray = splitBy(select.toString(), "[,]");
        List<QueryColumn> selectedColumnList = query.getColumnList();
        addColumnTo(selectedColumnList, selectArray);
        if (log.isDebugEnabled()) log.debug("SelectedColumnList: {}", Arrays.toString(selectedColumnList.toArray()));

        /*from => tableList*/
        String[] fromArray = splitBy(from.toString(), "[,]|([Ff][Uu][Ll][Ll] |[Ll][Ee][Ff][Tt] |[Rr][Ii][Gg][Hh][Tt] )*([Ii][Nn][Nn][Ee][Rr] |[Oo][Uu][Tt][Ee][Rr] )*([Jj][Oo][Ii][Nn])");
        List<QueryTable> tableList = query.getTableList();
        addTableTo(tableList, fromArray, selectedColumnList);
        if (log.isDebugEnabled()) log.debug("TableList: {}", Arrays.toString(tableList.toArray()));

        /*where => filterList*/
        String[] whereArray = splitBy(where.toString(), "[Aa][Nn][Dd]|[Oo][Rr]");
        List<QueryFilter> filterList = query.getFilterList();
        addFilterTo(filterList, whereArray);
        if (log.isDebugEnabled()) log.debug("FilterList: {}", Arrays.toString(filterList.toArray()));

        /*oder by => sortList*/
        String[] orderByArray = splitBy(orderBy.toString(), "[,]");
        List<QuerySort> sortList = query.getSortList();
        addSortTo(sortList, orderByArray);
        if (log.isDebugEnabled()) log.debug("SortList: {}", Arrays.toString(sortList.toArray()));

        // save Query Data
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();
        int stepId = project.getActiveStep().getId();
        saveQuery(query, stepId, mapper, dataManager, projectUser);

        // save DataFile
        dataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), projectUser, dataFile.getId(), stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }

    private void saveQuery(Query query, int stepId, ProjectMapper mapper, DataManager dataManager, ProjectUser projectUser) {
        int queryId = query.getId();
        dataManager.addData(ProjectFileType.QUERY, mapper.map(query), projectUser, queryId, stepId, String.valueOf(queryId));

        /*QUERY_TABLE_LIST*/
        dataManager.addData(ProjectFileType.QUERY_TABLE_LIST, mapper.fromQueryTableList(query.getTableList()), projectUser, queryId, stepId, String.valueOf(queryId));

        /*QUERY_TABLE*/
        List<QueryTable> tableList = query.getTableList();
        int tableId;
        for (QueryTable queryTable : tableList) {
            tableId = queryTable.getId();
            dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(queryTable), projectUser, tableId, stepId, (queryId + "/" + tableId));

            /*QUERY_COLUMN_LIST*/
            dataManager.addData(ProjectFileType.QUERY_COLUMN_LIST, mapper.fromQueryColumnList(queryTable.getColumnList()), projectUser, queryId, stepId, (queryId + "/" + tableId));

            /*QUERY_COLUMN*/
            List<QueryColumn> columnList = queryTable.getColumnList();
            int columnId;
            for (QueryColumn queryColumn : columnList) {
                columnId = queryColumn.getId();
                dataManager.addData(ProjectFileType.QUERY_COLUMN, mapper.map(queryColumn), projectUser, columnId, stepId, (queryId + "/" + tableId));
            }
        }

        /*QUERY_FILTER_LIST*/
        dataManager.addData(ProjectFileType.QUERY_FILTER_LIST, mapper.fromQueryFilterList(query.getFilterList()), projectUser, queryId, stepId, String.valueOf(queryId));

        /*QUERY_FILTER*/
        List<QueryFilter> filterList = query.getFilterList();
        QueryFilterData queryFilterData;
        int filterId;
        for (QueryFilter queryFilter : filterList) {
            filterId = queryFilter.getId();
            dataManager.addData(ProjectFileType.QUERY_FILTER, mapper.map(queryFilter), projectUser, filterId, stepId, String.valueOf(queryId));
        }

        /*QUERY_SORT_LIST*/
        dataManager.addData(ProjectFileType.QUERY_SORT_LIST, mapper.fromQuerySortList(query.getSortList()), projectUser, queryId, stepId, String.valueOf(queryId));

        /*QUERY_SORT*/
        QuerySortData querySortData;
        int sortId;
        for (QuerySort querySort : query.getSortList()) {
            sortId = querySort.getId();
            dataManager.addData(ProjectFileType.QUERY_SORT, mapper.map(querySort), projectUser, sortId, stepId, String.valueOf(queryId));
        }
    }

    private void addFilterTo(List<QueryFilter> filterList, String[] whereArray) {
        /*first condition need connector same as other condition*/
        whereArray[0] = "AND " + whereArray[0];
        StringBuilder operation;
        String connector;
        int operationIndex;
        for (String where : whereArray) {
            connector = where.substring(0, 3).trim().toUpperCase();
            operation = new StringBuilder();
            operationIndex = findOperation(where, operation);
            QueryFilter queryFilter = new QueryFilter(
                    connector,
                    where.substring(3, operationIndex).trim(),
                    operation.toString(),
                    where.substring(operationIndex + operation.length()).trim()
            );
            filterList.add(queryFilter);
        }
    }

    private int findOperation(String where, StringBuilder operation) {
        char[] one = {'=', '>', '<', '!'};
        char[] second = {'S', 'N'};

        where = where.toUpperCase();
        char[] chars = where.toCharArray();
        char ch;
        char next;
        int operLength = 0;
        int operStart = 0;
        for (int index = 0; index < chars.length; index++) {
            ch = chars[index];
            if (match(ch, one)) {
                /*[ =, >, <, <>, !=, >=, <= ]*/
                next = chars[index + 1];
                operLength = (next == '=' || next == '>') ? 2 : 1;
                operStart = index;
                break;

            } else if (ch == 'I') {
                /*[ IS, IN, IS NOT ]*/
                next = chars[index + 1];
                if (match(next, second)) {
                    next = chars[index + 2];
                    if (next == ' ') {
                        if (where.substring(index, index + 6).equals("IS NOT")) {
                            operLength = 6;
                            operStart = index;
                            break;
                        } else {
                            /*[ IS, IN ]*/
                            operLength = 2;
                            operStart = index;
                            break;
                        }
                    }
                }

            } else if (ch == 'N') {
                if (where.substring(index, index + 6).equals("NOT IN")) {
                    operLength = 6;
                    operStart = index;
                    break;
                } else if (where.substring(index, index + 8).equals("NOT LIKE")) {
                    operLength = 8;
                    operStart = index;
                    break;
                }

            } else if (ch == 'L') {
                if (where.substring(index, index + 4).equals("LIKE")) {
                    operLength = 4;
                    operStart = index;
                    break;
                }
            }
        } // end of for

        operation.append(where, operStart, operStart + operLength);
        return operStart;
    }

    private boolean match(char ch, char[] chars) {
        for (char aChar : chars) {
            if (ch == aChar) {
                return true;
            }
        }
        return false;
    }

    private void addTableTo(List<QueryTable> tableList, String[] fromArray, List<QueryColumn> selectedColumnList) {
        QueryTable queryTable;
        StringBuilder tableName;
        StringBuilder tableAlias;
        StringBuilder tableJoinType;
        StringBuilder joinedTableName;
        StringBuilder joinCondition;
        String[] words;
        String upperCase;
        for (String table : fromArray) {
            words = table.trim().split("[,][ ]|[ ,()=]");
            upperCase = words[0].toUpperCase();
            if (upperCase.isEmpty() || !"INNER|LEFT|RIGHT|FULL|OUTER".contains(upperCase)) {
                for (String word : words) {
                    queryTable = new QueryTable(word);
                    tableList.add(queryTable);
                    loadColumnList(queryTable);
                    markSelectedColumn(queryTable, selectedColumnList);
                }

            } else {
                tableName = new StringBuilder();
                tableAlias = new StringBuilder();
                tableJoinType = new StringBuilder();
                joinedTableName = new StringBuilder();
                joinCondition = new StringBuilder();
                splitTableWithJoin(table, words, tableName, tableAlias, tableJoinType, joinedTableName, joinCondition);

                queryTable = new QueryTable(tableName.toString(), tableAlias.toString(), tableJoinType.toString(), joinedTableName.toString(), joinCondition.toString());
                tableList.add(queryTable);
                loadColumnList(queryTable);
                markSelectedColumn(queryTable, selectedColumnList);
                QueryTable joinTable = findTable(queryTable.getJoinTable(), tableList);
                queryTable.setJoinTableId(joinTable == null ? 0 : joinTable.getId());
            }
        }
        tableList.sort(Comparator.comparing(QueryTable::getName));
    }

    private void addColumnTo(List<QueryColumn> selectedColumnList, String[] selectArray) {
        QueryColumn queryColumn;
        ColumnType type;
        String[] values;
        String name;
        String value;
        String uppercase;
        int compute = 0;
        int index = 0;
        for (String column : selectArray) {
            uppercase = column.toUpperCase();
            if (uppercase.replaceAll("\\s*[,]*\\s*[A-Z_]+[.][A-Z_]+\\s*(AS\\s*[A-Z_]+\\s*)*", "").isEmpty()) {
                if (uppercase.contains("AS")) {
                    type = ColumnType.ALIAS;
                    values = column.split("[Aa][Ss]");
                    name = values[1];
                    value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
                } else {
                    type = ColumnType.NORMAL;
                    values = column.split("[.]");
                    name = values[1];
                    value = column.startsWith(",") ? column.substring(1) : column;
                }
            } else if (uppercase.contains("AS")) {
                type = ColumnType.COMPUTE;
                values = column.split("[Aa][Ss]");
                name = values[1];
                value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
            } else {
                type = ColumnType.COMPUTE;
                name = "COMPUTE" + (++compute);
                value = column.startsWith(",") ? column.substring(1) : column;
            }

            queryColumn = new QueryColumn(index++, ProjectUtil.newUniqueId(project), name, null);
            queryColumn.setType(type);
            queryColumn.setValue(value);
            queryColumn.setSelected(true);
            selectedColumnList.add(queryColumn);
        }
    }

    private void addSortTo(List<QuerySort> sortList, String[] sortArray) {
        sortArray[0] = ", " + sortArray[0];
        QuerySort querySort;
        int index = 0;
        for (String sort : sortArray) {
            querySort = new QuerySort(index++, ProjectUtil.newUniqueId(project), sort.substring(1).trim());
            sortList.add(querySort);
        }
    }

    private void markSelectedColumn(QueryTable queryTable, List<QueryColumn> selectedColumnList) {
        String tableName = queryTable.getName().toUpperCase();
        for (QueryColumn selected : selectedColumnList) {
            if (selected.getType() != ColumnType.COMPUTE) {
                if (tableName.equals(queryTable.getName().toUpperCase())) {
                    selected.setOwner(queryTable);

                    String selectedName = selected.getName().toUpperCase();
                    QueryColumn column = findColumn(selectedName, queryTable);
                    if (column != null) column.setSelected(true);
                }
            }
        }
    }

    private void loadColumnList(QueryTable queryTable) {
        String tableName = queryTable.getName().toUpperCase();

        /*load table list from Database using DConvers*/
        int dataSourceId = dataFile.getDataSourceId();
        Database database = project.getDatabaseMap().get(dataSourceId);
        String shortenDBMS = database.getDbms().name().split("[_]")[0].toLowerCase();
        Properties configs = workspace.getConfigs("dconvers." + shortenDBMS + ".");

        DConversHelper dConvers = new DConversHelper();
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);
        dConvers.addSourceTable("columns", 1, dataSourceName, configs.getProperty("sql.columns"), "");
        dConvers.addVariable("schema", database.getSchema());
        dConvers.addVariable("table", tableName);
        if (!dConvers.run()) {
            throw new UnsupportedOperationException("Load Column List Failed by DConvers!");
        }

        /*first column must be column-name*/
        List<QueryColumn> columnList = queryTable.getColumnList();
        DataTable tables = dConvers.getSourceTable("columns");
        int index = 0;
        for (DataRow row : tables.getRowList()) {
            DataColumn column = row.getColumn(0);
            columnList.add(new QueryColumn(index++, ProjectUtil.newUniqueId(project), column.getValue(), queryTable));
        }
    }

    private QueryColumn findColumn(String columnName, QueryTable queryTable) {
        columnName = columnName.toUpperCase();
        for (QueryColumn column : queryTable.getColumnList()) {
            if (columnName.equals(column.getName().toLowerCase())) {
                return column;
            }
        }
        return null;
    }

    private QueryTable findTable(String tableName, List<QueryTable> tableList) {
        tableName = tableName.toUpperCase();
        for (QueryTable table : tableList) {
            if (tableName.equals(table.getName().toLowerCase())) {
                return table;
            }
        }
        return null;
    }

    private void splitTableWithJoin(String table, String[] words, StringBuilder tableName, StringBuilder tableAlias, StringBuilder tableJoinType, StringBuilder joinedTableName, StringBuilder joinCondition) {
        /*this is one of JOIN Type and condition always appear after word 'ON'*/
        joinCondition.append(table.split("[Oo][Nn]")[1]);

        /*find JOIN Type and Table Name*/
        String upperCase;
        int wordCount = words.length;
        int next = 0;
        tableJoinType.append(words[0].toUpperCase());
        for (int i = 1; i < wordCount; i++) {
            upperCase = words[i].toUpperCase();
            tableJoinType.append("_").append(upperCase);
            if ("JOIN".equals(upperCase)) {
                tableName.append(words[i + 1]);
                if ("ON".equals(words[i + 2].toUpperCase())) {
                    tableAlias.append(words[i + 1]);
                } else {
                    tableAlias.append(words[i + 2]);
                }
                next = i + 3;
                break;
            }
        }

        /*find Joined Table Name*/
        String tableNameString = tableName.toString();
        String tableAliasString = tableAlias.toString();
        for (int i = next; i < wordCount; i++) {
            if (words[i].contains(".")) {
                /*this is table-name.column-name*/
                String[] tableColumn = words[i].split("[.]");
                if (!tableColumn[0].equalsIgnoreCase(tableNameString) && !tableColumn[0].equalsIgnoreCase(tableAliasString)) {
                    joinedTableName.append(tableColumn[0]);
                    break;
                }
            }
        }
    }

    private String[] splitBy(String source, String splitters) {
        String splitter = "__SPLITTER__";
        source = source.replaceAll("(" + splitters + ")", splitter + "$1");
        return source.split(splitter);
    }

    private void splitSQLPart(String sql, StringBuilder select, StringBuilder from, StringBuilder where, StringBuilder orderBy) {
        String original = sql;
        sql = original.toUpperCase();

        /*indexes*/
        int selectIndex = sql.indexOf("SELECT");
        int fromIndex = sql.indexOf("FROM");
        int whereIndex = sql.indexOf("WHERE");
        int orderIndex = sql.indexOf("ORDER BY");

        /*select*/
        select.append(original.substring(selectIndex + 6, fromIndex));

        /*from*/
        int endIndex = whereIndex > 0 ? whereIndex : (orderIndex > 0 ? orderIndex : -1);
        from.append(endIndex > 0 ? original.substring(fromIndex + 4, endIndex) : original.substring(fromIndex + 4));

        /*where*/
        if (whereIndex > 0) {
            where.append((orderIndex > 0) ? original.substring(whereIndex + 5, orderIndex) : original.substring(whereIndex + 5));
        }

        /*orderBy*/
        orderBy.append((orderIndex > 0) ? original.substring(orderIndex + 8) : "");

        /*TODO: future feature: need support GROUP and HAVING*/
    }

}
