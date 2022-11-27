package com.tflow.controller;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.system.Properties;
import com.tflow.util.DConversHelper;
import com.tflow.util.FacesUtil;
import com.tflow.util.HelperMap;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;

@ViewScoped
@Named("sqlQueryCtl")
public class SQLQueryController extends Controller {

    private DataFile dataFile;
    private List<QueryTable> tableList;

    private Query query;
    private Selectable selectableFilter;
    private Selectable selectableSort;
    private Selectable selectablePreview;

    private String openSectionUpdate;

    public String getOpenSectionUpdate() {
        return openSectionUpdate;
    }

    public void setOpenSectionUpdate(String openSectionUpdate) {
        this.openSectionUpdate = openSectionUpdate;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public List<QueryTable> getTableList() {
        return tableList;
    }

    public void setTableList(List<QueryTable> tableList) {
        this.tableList = tableList;
    }

    public Selectable getSelectableFilter() {
        return selectableFilter;
    }

    public void setSelectableFilter(Selectable selectableFilter) {
        this.selectableFilter = selectableFilter;
    }

    public Selectable getSelectableSort() {
        return selectableSort;
    }

    public void setSelectableSort(Selectable selectableSort) {
        this.selectableSort = selectableSort;
    }

    public Selectable getSelectablePreview() {
        return selectablePreview;
    }

    public void setSelectablePreview(Selectable selectablePreview) {
        this.selectablePreview = selectablePreview;
    }

    @Override
    public Page getPage() {
        return workspace.getCurrentPage();
    }

    @Override
    void onCreation() {
        dataFile = (DataFile) workspace.getProject().getActiveStep().getActiveObject();
        query = new Query();
    }

    public void clientReady() {
        log.debug("clientReady:fromClient");
        openSectionUpdate = openQuerySection();
        jsBuilder.post(JavaScript.unblockScreen).runOnClient();
    }

    /*TODO: each table need Alias-Name from Oracle Synonym,
        and need to show Alias-Name instead of Name*/
    private void reloadTableList() {
        jsBuilder.pre(JavaScript.blockScreenWithText, "LOADING TABLE LIST ...").runOnClient();
        log.debug("reloadTableList.");
        tableList = new ArrayList<>();

        /*load table list from Database using DConvers*/
        Project project = workspace.getProject();
        int dataSourceId = dataFile.getDataSourceId();
        Database database = workspace.getProject().getDatabaseMap().get(dataSourceId);
        Dbms dbms = database.getDbms();
        Properties configs = getDBMSConfigs(dbms);
        String schemas = quotedArray(query.getSchemaList(), dbms.getValueQuote());

        DConversHelper dConvers = new DConversHelper();
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);
        String dConversTableName = "tables";
        dConvers.addSourceTable(dConversTableName, 1, dataSourceName, configs.getProperty("sql.tables"), "");
        dConvers.addConsoleOutput(dConversTableName);
        dConvers.addVariable("schema", schemas);
        if (!dConvers.run()) {
            String message = "Load Table List Failed! please investigate in application log";
            jsBuilder.pre(JavaScript.notiWarn, message);
            tableList = new ArrayList<>();
            log.error(message);
            return;
        }

        /*first column must be table-name and second column must be schema-name*/
        List<QueryTable> selectedList = query.getTableList();
        DataTable tables = dConvers.getSourceTable(dConversTableName);
        String tableName;
        String schemaName;
        boolean isSelected;
        int index = 0;
        for (DataRow row : tables.getRowList()) {
            tableName = row.getColumn(0).getValue();
            schemaName = row.getColumn(1).getValue();

            /*exclude all tables that already selected in the Query*/
            isSelected = false;
            for (QueryTable selectedTable : selectedList) {
                if (selectedTable.getSchema().equalsIgnoreCase(schemaName) && selectedTable.getName().equalsIgnoreCase(tableName)) {
                    isSelected = true;
                    break;
                }
            }

            if (isSelected) continue;
            tableList.add(new QueryTable(index++, tableName, schemaName));
        }

        tableList.sort(Comparator.comparing(QueryTable::getName));
        if (log.isDebugEnabled()) log.debug("reloadTableList: completed, tableList: {}", Arrays.toString(tableList.toArray()));

    }

    private String quotedArray(List<String> schemaList, String quoteSymbol) {
        StringBuilder result = new StringBuilder();
        for (String schema : schemaList) {
            schema = "," + quoteSymbol + schema.trim().toUpperCase();
            if (!result.toString().contains(schema)) result.append(schema).append(quoteSymbol);
        }
        return result.length() == 0 ? quoteSymbol + quoteSymbol : result.substring(1);
    }

    private Properties getDBMSConfigs(Dbms dbms) {
        String shortenDBMS = dbms.name().split("[_]")[0].toLowerCase();
        return workspace.getConfigs("dconvers." + shortenDBMS + ".");
    }

    private void reloadSchemaList() {
        jsBuilder.pre(JavaScript.blockScreenWithText, "LOADING ALL SCHEMAS ...").runOnClient();
        log.debug("reloadSchemaList.");
        List<String> schemaList = query.getAllSchemaList();
        schemaList.clear();

        /*load table list from Database using DConvers*/
        Project project = workspace.getProject();
        int dataSourceId = dataFile.getDataSourceId();
        Database database = workspace.getProject().getDatabaseMap().get(dataSourceId);
        Dbms dbms = database.getDbms();
        Properties configs = getDBMSConfigs(dbms);

        DConversHelper dConvers = new DConversHelper();
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);
        String dConversTableName = "schemas";
        dConvers.addSourceTable(dConversTableName, 1, dataSourceName, configs.getProperty("sql.schemas"), "");
        if (!dConvers.run()) {
            String message = "Load Schema List Failed! please investigate in application log";
            jsBuilder.pre(JavaScript.notiWarn, message);
            log.error(message);
            return;
        }

        /*first column must be schema-name*/
        DataTable tables = dConvers.getSourceTable(dConversTableName);
        for (DataRow row : tables.getRowList()) {
            DataColumn column = row.getColumn(0);
            schemaList.add(column.getValue());
        }
        schemaList.sort(String::compareTo);
        if (log.isDebugEnabled()) log.debug("reloadSchemaList: completed, schemaList: {}", Arrays.toString(schemaList.toArray()));
    }

    private void reloadQuery() {
        jsBuilder.pre(JavaScript.blockScreenWithText, "LOADING QUERY ...").runOnClient();
        HelperMap<String, Object> propertyMap = new HelperMap(dataFile.getPropertyMap());
        int queryId = propertyMap.getInteger(PropertyVar.queryId.name(), 0);
        try {
            query = workspace.getProjectManager().loadQuery(queryId, workspace.getProject());
            query.setOwner(dataFile);
        } catch (Exception ex) {
            jsBuilder.pre(JavaScript.notiWarn, "Load Query Failed! {}", ex.getMessage());
            log.error("{}", ex.getMessage());
            log.trace("", ex);
            query = new Query();
            return;
        }

        selectableFilter = new SelectableReady() {
            String selectableId = IDPrefix.QUERY_FILTER.getPrefix() + query.getId();

            @Override
            public com.tflow.model.editor.Properties getProperties() {
                return com.tflow.model.editor.Properties.QUERY_FILTER;
            }

            @Override
            public String getSelectableId() {
                return selectableId;
            }
        };
        selectableSort = new SelectableReady() {
            String selectableId = IDPrefix.QUERY_SORT.getPrefix() + query.getId();

            @Override
            public com.tflow.model.editor.Properties getProperties() {
                return com.tflow.model.editor.Properties.QUERY_SORT;
            }

            @Override
            public String getSelectableId() {
                return selectableId;
            }
        };
        selectablePreview = new SelectableReady() {
            String selectableId = IDPrefix.QUERY_PREVIEW.getPrefix() + query.getId();

            @Override
            public com.tflow.model.editor.Properties getProperties() {
                return com.tflow.model.editor.Properties.QUERY_PREVIEW;
            }

            @Override
            public String getSelectableId() {
                return selectableId;
            }
        };

        Map<String, Selectable> selectableMap = getStep().getSelectableMap();
        selectableMap.put(query.getSelectableId(), query);
        selectableMap.put(selectableFilter.getSelectableId(), selectableFilter);
        selectableMap.put(selectableSort.getSelectableId(), selectableSort);
        selectableMap.put(selectablePreview.getSelectableId(), selectablePreview);
    }

    private void selectQuery(Selectable selectable) {
        if (selectable instanceof Query) {
            Query query = (Query) selectable;
            if (query.getAllSchemaList().size() == 0) reloadSchemaList();
            query.refreshQuickColumnList();
        }

        jsBuilder
                .pre(JavaScript.selectObject, selectable.getSelectableId())
                .runOnClient(true);
    }

    public void openSection(TabChangeEvent event) {
        String title = event.getTab().getTitle();
        SQLQuerySection section = SQLQuerySection.parse(title);
        if (section == null) {
            String message = "Unknown section with title: {}";
            jsBuilder.pre(JavaScript.notiError, message, title);
            log.error(message, title);
            return;
        }

        switch (section) {
            case QUERY:
                openSectionUpdate = openQuerySection();
                break;
            case SQL:
                openSectionUpdate = openPreviewSection();
                break;
            case FILTER:
                openSectionUpdate = openFilterSection();
                break;
            case SORT:
                openSectionUpdate = openSortSection();
                break;
        }
        jsBuilder.post(JavaScript.unblockScreen).runOnClient();
    }

    private String openQuerySection() {
        if (query == null || query.getId() == 0) reloadQuery();
        selectQuery(query);
        if (tableList == null) reloadTableList();
        return SQLQuerySection.QUERY.getUpdate();
    }

    private String openPreviewSection() {
        selectQuery(selectablePreview);

        /*TODO: generate preview*/

        return SQLQuerySection.SQL.getUpdate();
    }

    private String openFilterSection() {
        selectQuery(selectableFilter);
        return SQLQuerySection.FILTER.getUpdate();
    }

    private String openSortSection() {
        selectQuery(selectableSort);
        return SQLQuerySection.SORT.getUpdate();
    }

    public void columnSelected() {
        String param = FacesUtil.getRequestParam("columnId");
        String selected = FacesUtil.getRequestParam("selected");
        if (param == null || selected == null) {
            log.error("columnSelected:fromClient requires columnId and selected!");
            jsBuilder.pre(JavaScript.notiError, "Can not perform select action, invalid parameters!");
            return;
        }
        log.debug("columnSelected:fromClient");
        boolean selectAll = !param.contains(".");

        int tableId;
        int columnId;
        if (selectAll) {
            tableId = Integer.parseInt(param);
            columnId = 0;
        } else {
            String[] params = param.split("[.]");
            tableId = Integer.parseInt(params[0]);
            columnId = Integer.parseInt(params[1]);
        }

        QueryTable table = getTable(tableId, query.getTableList());
        if (table == null) {
            log.error("invalid table-id:{}", tableId);
            jsBuilder.pre(JavaScript.notiError, "Can not perform select action, invalid table-id:{}!", tableId);
            return;
        }

        List<QueryColumn> columnList = table.getColumnList();
        QueryColumn column;
        if (selectAll) {
            column = columnList.get(0);
        } else {
            column = getColumn(columnId, columnList);
            if (column == null) {
                log.error("invalid column-id:{}", columnId);
                jsBuilder.pre(JavaScript.notiError, "Can not perform select action, invalid column-id:{}!", columnId);
                return;
            }
        }

        Step step = getStep();
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.SWITCH_ON, selectAll);
        paramMap.put(CommandParamKey.QUERY, query);
        paramMap.put(CommandParamKey.STEP, step);

        Action action;
        if (Boolean.parseBoolean(selected)) {
            paramMap.put(CommandParamKey.QUERY_COLUMN, column);
            action = new AddQueryColumn(paramMap);
        } else {
            paramMap.put(CommandParamKey.COLUMN_ID, selectAll ? table.getId() : column.getId());
            action = new RemoveQueryColumn(paramMap);
        }

        try {
            action.execute();
            query.refreshQuickColumnList();
        } catch (Exception ex) {
            String message = "Action {} failed! {}:{}";
            jsBuilder.pre(JavaScript.notiError, message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.error(message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.trace("", ex);
        }
    }

    private QueryColumn getColumn(int id, List<QueryColumn> columnList) {
        for (QueryColumn column : columnList) {
            if (column.getId() == id) {
                return column;
            }
        }
        return null;
    }

    private QueryTable getTable(int id, List<QueryTable> tableList) {
        for (QueryTable table : tableList) {
            if (table.getId() == id) {
                return table;
            }
        }
        return null;
    }

    public void sortColumns() {
        log.debug("sortColumns:fromtClient");
        boolean byName = Boolean.parseBoolean(FacesUtil.getRequestParam("byName"));
        boolean maxFirst = Boolean.parseBoolean(FacesUtil.getRequestParam("maxFirst"));
        int tableId = Integer.parseInt(FacesUtil.getRequestParam("tableId"));
        log.debug("tableId:{}, byName:{}, maxFIrst:{}", tableId, byName, maxFirst);

        QueryTable queryTable = getTable(tableId, query.getTableList());
        if (queryTable == null) {
            jsBuilder.pre(JavaScript.notiError, "Table({}) not found!", tableId);
            return;
        }

        if (byName) {
            if (maxFirst) {
                queryTable.getColumnList().sort((c1, c2) -> {
                    return c2.getName().compareTo(c1.getName());
                });
            } else {
                queryTable.getColumnList().sort(Comparator.comparing(QueryColumn::getName));
            }

        } else /*byIndex*/ {
            if (maxFirst) {
                queryTable.getColumnList().sort((c1, c2) -> {
                    return Integer.compare(c2.getIndex(), c1.getIndex());
                });
            } else {
                queryTable.getColumnList().sort(Comparator.comparingInt(QueryColumn::getIndex));
            }
        }
    }

    public void addTable(QueryTable queryTable) {
        log.debug("addTable(table:{})", queryTable);
        int index = tableList.indexOf(queryTable);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.QUERY, query);
        paramMap.put(CommandParamKey.QUERY_TABLE, queryTable);
        paramMap.put(CommandParamKey.WORKSPACE, workspace);
        AddQueryTable action = new AddQueryTable(paramMap);

        try {
            action.execute();
            tableList.remove(index);
        } catch (Exception ex) {
            String message = "Action {} failed! {}:{}";
            jsBuilder.pre(JavaScript.notiError, message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.error(message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.trace("", ex);
        }
    }

    public void removeTable(QueryTable queryTable) {
        log.debug("removeTable(table:{})", queryTable);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.QUERY, query);
        paramMap.put(CommandParamKey.QUERY_TABLE, queryTable);
        paramMap.put(CommandParamKey.WORKSPACE, workspace);
        RemoveQueryTable action = new RemoveQueryTable(paramMap);

        try {
            action.execute();
        } catch (Exception ex) {
            String message = "Action {} failed! {}:{}";
            jsBuilder.pre(JavaScript.notiError, message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.error(message, action.getName(), ex.getClass().getSimpleName(), ex.getMessage());
            log.trace("", ex);
        }
    }

}
