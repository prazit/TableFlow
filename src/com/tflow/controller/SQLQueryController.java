package com.tflow.controller;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.sql.Query;
import com.tflow.system.Properties;
import com.tflow.util.DConversHelper;
import com.tflow.util.HelperMap;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("sqlQueryCtl")
public class SQLQueryController extends Controller {

    private DataFile dataFile;
    private List<String> tableList;

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

    public List<String> getTableList() {
        return tableList;
    }

    public void setTableList(List<String> tableList) {
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

        /*first column must be table-name*/
        DataTable tables = dConvers.getSourceTable(dConversTableName);
        for (DataRow row : tables.getRowList()) {
            DataColumn column = row.getColumn(0);
            tableList.add(column.getValue());
        }
        tableList.sort(String::compareTo);
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

}
