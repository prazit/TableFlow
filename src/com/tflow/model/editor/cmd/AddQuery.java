package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.data.query.ColumnType;
import com.tflow.model.data.query.QueryFilterData;
import com.tflow.model.data.query.QuerySortData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.editor.sql.*;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class AddQuery extends QueryCommand {

    private Logger log;
    private Workspace workspace;
    private Project project;
    private DataFile dataFile;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        log = LoggerFactory.getLogger(getClass());

        workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        project = workspace.getProject();
        dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        BinaryFile sqlFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);

        Query query = new Query();
        query.setId(ProjectUtil.newUniqueId(project));
        query.setName(sqlFile.getName());
        dataFile.getPropertyMap().put(PropertyVar.queryId.name(), query.getId());

        Step step = project.getActiveStep();
        Tower tower = new Tower(ProjectUtil.newUniqueId(project), 0, step);
        query.setTower(tower);

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
        String[] selectArray = splitColumn(select.toString());
        List<QueryColumn> selectedColumnList = query.getColumnList();
        addColumnTo(selectedColumnList, selectArray);
        if (log.isDebugEnabled()) log.debug("SelectedColumnList: {}", Arrays.toString(selectedColumnList.toArray()));

        /*from => tableList*/
        String[] fromArray = splitBy(from.toString(), "[,]|([Ff][Uu][Ll][Ll] |[Ll][Ee][Ff][Tt] |[Rr][Ii][Gg][Hh][Tt] )*([Ii][Nn][Nn][Ee][Rr] |[Oo][Uu][Tt][Ee][Rr] )*([Jj][Oo][Ii][Nn])");
        List<QueryTable> tableList = query.getTableList();
        addTableTo(tableList, fromArray, selectedColumnList, tower);
        addSchemaTo(query.getSchemaList(), tableList);
        if (log.isDebugEnabled()) log.debug("TableList: {}", Arrays.toString(tableList.toArray()));

        /*where => filterList*/
        String[] whereArray = splitBy(where.toString(), "[Aa][Nn][Dd]|[Oo][Rr]");
        List<QueryFilter> filterList = query.getFilterList();
        addFilterTo(filterList, whereArray);
        if (log.isDebugEnabled()) log.debug("FilterList: {}", Arrays.toString(filterList.toArray()));

        /*oder by => sortList*/
        String[] orderByArray = splitColumn(orderBy.toString());
        List<QuerySort> sortList = query.getSortList();
        addSortTo(sortList, orderByArray);
        if (log.isDebugEnabled()) log.debug("SortList: {}", Arrays.toString(sortList.toArray()));

        /*correction#1: select tableA.* need all column from tableA*/
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        correctSelectAll(query, mapper);

        /*not update selectableMap, lets SQLQueryController do it*/


        // save Query Data
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();
        int stepId = step.getId();
        saveQuery(query, stepId, mapper, dataManager, projectUser);

        // save DataFile
        dataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), projectUser, dataFile.getId(), stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }

    private void addSchemaTo(List<String> schemaList, List<QueryTable> tableList) {
        StringBuilder alreadyAdded = new StringBuilder();
        String schema;
        for (QueryTable table : tableList) {
            schema = table.getSchema();
            if (schema == null) continue;
            if (!alreadyAdded.toString().contains("," + schema)) {
                alreadyAdded.append(",").append(schema);
                schemaList.add(schema);
            }
        }
        schemaList.sort(String::compareTo);
    }

    private void correctSelectAll(Query query, ProjectMapper mapper) {
        List<QueryColumn> columnList = query.getColumnList();
        QueryColumn queryColumn;
        QueryColumn newColumn;
        QueryTable queryTable;
        String tableName;
        for (int index = 0; index < columnList.size(); index++) {

            /*find column with star symbol*/
            queryColumn = columnList.get(index);
            if (!queryColumn.getName().equals("*")) continue;

            /*find table from found-column*/
            tableName = queryColumn.getValue().split("[.]")[0];
            queryTable = findTable(tableName, query.getTableList());

            /*remove found-column before insert columns*/
            columnList.remove(index);

            /*insert all column from found-table*/
            for (QueryColumn column : queryTable.getColumnList()) {
                column.setSelected(true);
                newColumn = new QueryColumn();
                mapper.copy(column, newColumn);
                newColumn.setId(ProjectUtil.newUniqueId(project));
                newColumn.setSelected(true);
                columnList.add(index++, newColumn);
            }

        }
    }

    private void saveQuery(Query query, int stepId, ProjectMapper mapper, DataManager dataManager, ProjectUser projectUser) {
        int queryId = query.getId();
        String childId = String.valueOf(queryId);
        dataManager.addData(ProjectFileType.QUERY, mapper.map(query), projectUser, queryId, stepId, childId);

        /*QUERY_TOWER*/
        Tower tower = query.getTower();
        dataManager.addData(ProjectFileType.QUERY_TOWER, mapper.map(tower), projectUser, tower.getId(), stepId, childId);

        /*QUERY_TABLE_LIST*/
        dataManager.addData(ProjectFileType.QUERY_TABLE_LIST, mapper.fromQueryTableList(query.getTableList()), projectUser, queryId, stepId, childId);

        /*QUERY_TABLE*/
        List<QueryTable> tableList = query.getTableList();
        int tableId;
        for (QueryTable queryTable : tableList) {
            tableId = queryTable.getId();
            dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(queryTable), projectUser, tableId, stepId, childId);
        }

        /*QUERY_FILTER_LIST*/
        dataManager.addData(ProjectFileType.QUERY_FILTER_LIST, mapper.fromQueryFilterList(query.getFilterList()), projectUser, queryId, stepId, childId);

        /*QUERY_FILTER*/
        List<QueryFilter> filterList = query.getFilterList();
        QueryFilterData queryFilterData;
        int filterId;
        for (QueryFilter queryFilter : filterList) {
            filterId = queryFilter.getId();
            dataManager.addData(ProjectFileType.QUERY_FILTER, mapper.map(queryFilter), projectUser, filterId, stepId, childId);
        }

        /*QUERY_SORT_LIST*/
        dataManager.addData(ProjectFileType.QUERY_SORT_LIST, mapper.fromQuerySortList(query.getSortList()), projectUser, queryId, stepId, childId);

        /*QUERY_SORT*/
        QuerySortData querySortData;
        int sortId;
        for (QuerySort querySort : query.getSortList()) {
            sortId = querySort.getId();
            dataManager.addData(ProjectFileType.QUERY_SORT, mapper.map(querySort), projectUser, sortId, stepId, childId);
        }
    }

    private void addFilterTo(List<QueryFilter> filterList, String[] whereArray) {
        if (whereArray.length == 0) return;

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
                    ProjectUtil.newUniqueId(project),
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

    private void addTableTo(List<QueryTable> tableList, String[] fromArray, List<QueryColumn> selectedColumnList, Tower tower) {
        QueryTable queryTable;
        StringBuilder tableSchema;
        StringBuilder tableName;
        StringBuilder tableAlias;
        StringBuilder tableJoinType;
        StringBuilder joinedTableName;
        StringBuilder joinCondition;
        String[] words;
        String upperCase;
        int roomIndex = 0;
        for (String table : fromArray) {
            words = table.trim().split("[,][ ]|[ ,()=]");
            upperCase = words[0].toUpperCase();
            if (upperCase.isEmpty() || !"INNER|LEFT|RIGHT|FULL|OUTER".contains(upperCase)) {
                for (String word : words) {
                    if (word.contains(".")) {
                        String[] schemaName = word.split("[.]");
                        queryTable = new QueryTable(ProjectUtil.newUniqueId(project), schemaName[1]);
                        queryTable.setSchema(schemaName[0]);
                    } else {
                        queryTable = new QueryTable(ProjectUtil.newUniqueId(project), word);
                    }
                    tableList.add(queryTable);

                    loadColumnList(queryTable, dataFile, project, workspace);
                    markSelectedColumn(queryTable, selectedColumnList);

                    tower.setRoom(0, roomIndex++, queryTable);
                }

            } else {
                tableSchema = new StringBuilder();
                tableName = new StringBuilder();
                tableAlias = new StringBuilder();
                tableJoinType = new StringBuilder();
                joinedTableName = new StringBuilder();
                joinCondition = new StringBuilder();
                splitTableWithJoin(table, words, tableSchema, tableName, tableAlias, tableJoinType, joinedTableName, joinCondition);
                queryTable = new QueryTable(ProjectUtil.newUniqueId(project), tableSchema.toString(), tableName.toString(), tableAlias.toString(), tableJoinType.toString(), joinedTableName.toString(), joinCondition.toString());
                tableList.add(queryTable);

                loadColumnList(queryTable, dataFile, project, workspace);
                markSelectedColumn(queryTable, selectedColumnList);

                tower.setRoom(0, roomIndex++, queryTable);
            }
        }
        tableList.sort(Comparator.comparing(QueryTable::getName));

        /*need Table-ID for JoinedTable*/
        QueryTable joinTable;
        for (QueryTable table : tableList) {
            String joinTableName = table.getJoinTable();
            if (joinTableName.isEmpty()) continue;

            joinTable = findTable(joinTableName, tableList);
            table.setJoinTableId(joinTable == null ? 0 : joinTable.getId());
        }
    }

    private void addColumnTo(List<QueryColumn> selectedColumnList, String[] selectArray) {
        QueryColumn queryColumn;
        ColumnType type;
        String[] values;
        String name;
        String value;
        String uppercase;
        String normalNamePattern = "([.]*[A-Z_*][A-Z0-9_]+)+";
        int compute = 0;
        int index = 0;
        for (String column : selectArray) {
            uppercase = column.toUpperCase();
            if (uppercase.replaceAll("\\s*[,]*\\s*" + normalNamePattern + "(\\s+AS\\s+[A-Z0-9_]+\\s*|\\s*)", "").isEmpty()) {
                if (Pattern.compile("[\\s]AS[\\s]").matcher(uppercase).find()) {
                    type = ColumnType.ALIAS;
                    values = column.split("[\\s][Aa][Ss][\\s]");
                    name = values[1];
                    value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
                } else {
                    type = ColumnType.NORMAL;
                    values = column.split("[.]");
                    name = values[1];
                    value = column.startsWith(",") ? column.substring(1) : column;
                }
            } else if (Pattern.compile("[\\s]AS[\\s]").matcher(uppercase).find()) {
                type = ColumnType.COMPUTE;
                values = column.split("[\\s][Aa][Ss][\\s]");
                name = values[1];
                value = values[0].startsWith(",") ? values[0].substring(1) : values[0];
            } else {
                type = ColumnType.COMPUTE;
                name = "COMPUTE" + (++compute);
                value = column.startsWith(",") ? column.substring(1) : column;
            }

            queryColumn = new QueryColumn(index, index, name, null);
            queryColumn.setType(type);
            queryColumn.setValue(value.trim());
            queryColumn.setSelected(true);
            selectedColumnList.add(queryColumn);
            index++;
        }
    }

    private void addSortTo(List<QuerySort> sortList, String[] sortArray) {
        if (sortArray.length == 0) return;

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
                String[] tableColumn = selected.getValue().toUpperCase().split("[.]");
                if (tableName.equals(tableColumn[0])) {
                    selected.setOwner(queryTable);
                    QueryColumn column = findColumn(tableColumn[1], queryTable);
                    if (column != null) {
                        selected.setSelected(true);
                        selected.setId(column.getId());
                        selected.setOwner(column.getOwner());
                        column.setSelected(true);
                    }
                } else {
                    log.debug("markSelectedColumn: ignore different table({}) and selected-table({}), selected-column: {}", tableName, tableColumn[0], selected);
                }
            }
        }
    }

    private QueryColumn findColumn(String columnName, QueryTable queryTable) {
        for (QueryColumn column : queryTable.getColumnList()) {
            if (columnName.equals(column.getName().toUpperCase())) {
                return column;
            }
        }
        log.debug("findColumn: column({}) not found on table({})", columnName, queryTable.getName());
        // need null instead of throw new UnsupportedOperationException("Invalid Column Reference: '" + columnName + "' not found in table '" + queryTable.getName() + "'");
        return null;
    }

    private QueryTable findTable(String tableName, List<QueryTable> tableList) {
        tableName = tableName.toUpperCase();
        for (QueryTable table : tableList) {
            if (tableName.equals(table.getName().toUpperCase())) {
                return table;
            }
        }
        throw new UnsupportedOperationException("Invalid Table Reference: '" + tableName + "' not found in table list!");
    }

    private void splitTableWithJoin(String table, String[] words, StringBuilder tableSchema, StringBuilder tableName, StringBuilder tableAlias, StringBuilder tableJoinType, StringBuilder joinedTableName, StringBuilder joinCondition) {
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

        String tableNameString = tableName.toString();
        if (tableNameString.contains(".")) {
            String[] schemaName = tableNameString.split("[.]");
            tableSchema.append(schemaName[0]);
            tableNameString = schemaName[1];
            tableName.setLength(0);
            tableName.append(tableNameString);
        }
        String tableAliasString = tableAlias.toString();
        if (tableAliasString.contains(".")) {
            String[] schemaName = tableAliasString.split("[.]");
            tableAliasString = schemaName[1];
            tableAlias.setLength(0);
            tableAlias.append(tableAliasString);
        }

        /*find Joined Table Name*/
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

    private String[] splitColumn(String source) {
        /*split compute-column need different way (Remember: compute-column always need alias, need alert at the Extract process)*/
        List<String> columnList = new ArrayList<>();
        String replacedWord = "RE__PLACED__";

        /*collect all Quoted*/
        String quotedPattern = "[']([^']+)[']|[\\\"]([^\\\"]+)[\\\"]|[\\(]([^\\(\\)]+)[\\)]";
        LinkedHashMap<String, String> quotedMap = new LinkedHashMap();
        int count = 0;
        String replaced = source;
        while (true) {
            String key = replacedWord + (++count);
            String before = replaced;
            int lengthBefore = before.length();
            replaced = before.replaceFirst(quotedPattern, key);
            if (replaced.length() != lengthBefore) {
                int index = replaced.indexOf(key);
                String value = before.substring(index, index + (lengthBefore - replaced.length() + key.length()));
                quotedMap.put(key, value);
            } else {
                break;
            }
        }

        /*selector: (operator) (constant|name)*/
        String selector = "((\\s*[\\+\\-\\*\\/]|\\s*[\\|\\&]{2})*((\\s*['].*[']|\\s*[\"].*[\"]|\\s*[0-9]+([.][0-9]+)?)|(\\s*[A-Za-z][A-Za-z0-9_]*[.][A-Za-z][A-Za-z0-9_]*|\\s*[A-Za-z][A-Za-z0-9_]*)))+";
        String item = firstItem(replaced, selector);
        while (item != null) {
            replaced = replaced.replaceFirst(selector, "");
            columnList.add(item);
            item = firstItem(replaced, selector);
        }

        /*restore all value by replace all key*/
        String[] result = new String[columnList.size()];
        int index = 0;
        selector = replacedWord + "[0-9]+";
        String value;
        String key;
        for (String column : columnList) {
            key = firstItem(column, selector);
            while (key != null) {
                value = quotedMap.get(key);
                column = column.replaceFirst(selector, value);
                key = firstItem(column, selector);
            }
            result[index++] = column;
        }

        return result;
    }

    /**
     * find item in source by selector
     *
     * @return first item when found, otherwise return null
     */
    private String firstItem(String source, String selector) {
        String finder = "F__I__N__D__E__R";
        int lengthBefore = source.length();
        String replaced = source.replaceFirst(selector, finder);
        if (replaced.length() != lengthBefore) {
            int index = replaced.indexOf(finder);
            return source.substring(index, index + (lengthBefore - replaced.length() + finder.length()));
        }
        return null;
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
