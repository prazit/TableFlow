package com.tflow.controller;

import com.clevel.dconvers.data.DataColumn;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.data.DataTable;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.EditorType;
import com.tflow.model.editor.JavaScript;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.sql.Query;
import com.tflow.system.Properties;
import com.tflow.util.DConversHelper;
import com.tflow.util.HelperMap;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@Named("sqlQueryCtl")
public class SQLQueryController extends Controller {

    private DataFile dataFile;
    private List<String> tableList;
    private Query query;

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

    @Override
    public Page getPage() {
        return workspace.getCurrentPage();
    }

    @Override
    void onCreation() {
        dataFile = (DataFile) workspace.getProject().getActiveStep().getActiveObject();
        openSectionUpdate = openQuerySection();
        openSectionUpdate = openFilterSection();
        jsBuilder.post(JavaScript.unblockScreen).runOnClient();
    }

    private void reloadTableList() {
        tableList = new ArrayList<>();

        /*load table list from Database using DConvers*/
        Project project = workspace.getProject();
        int dataSourceId = dataFile.getDataSourceId();
        Database database = project.getDatabaseMap().get(dataSourceId);
        String shortenDBMS = database.getDbms().name().split("[_]")[0].toLowerCase();
        Properties configs = workspace.getConfigs("dconvers." + shortenDBMS + ".");

        DConversHelper dConvers = new DConversHelper();
        String dataSourceName = dConvers.addDatabase(dataSourceId, project);
        dConvers.addSourceTable("tables", 1, dataSourceName, configs.getProperty("sql.tables"), "");
        dConvers.addVariable("schema", database.getSchema());
        if (!dConvers.run()) {
            jsBuilder.pre(JavaScript.notiWarn, "Load Table List Failed! please investigate in application log");
            log.error("");
            tableList = new ArrayList<>();
            return;
        }

        /*first column must be table-name*/
        DataTable tables = dConvers.getSourceTable("tables");
        for (DataRow row : tables.getRowList()) {
            DataColumn column = row.getColumn(0);
            tableList.add(column.getValue());
        }
    }

    private void reloadQuery() {
        HelperMap<String, Object> propertyMap = new HelperMap(dataFile.getPropertyMap());
        int queryId = propertyMap.getInteger(PropertyVar.queryId.name(), 0);
        try {
            query = workspace.getProjectManager().loadQuery(queryId, workspace.getProject());
        } catch (Exception ex) {
            jsBuilder.pre(JavaScript.notiWarn, "Load Query Failed! {}", ex.getMessage());
            log.error("{}", ex.getMessage());
            log.trace("", ex);
            query = new Query();
        }
    }

    private void selectQuery() {
        jsBuilder
                .pre(JavaScript.selectObject, query.getSelectableId())
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
                openSectionUpdate = openSQLSection();
                break;
            case FILTER:
                openSectionUpdate = openFilterSection();
                break;
            case SORT:
                openSectionUpdate = openSortSection();
                break;
        }
    }

    private String openQuerySection() {
        reloadQuery();
        selectQuery();
        reloadTableList();
        return SQLQuerySection.QUERY.getUpdate();
    }

    private String openSQLSection() {

        /*TODO: load generated SQL*/

        return SQLQuerySection.SQL.getUpdate();
    }

    private String openFilterSection() {

        /*TODO: load Filters*/

        return SQLQuerySection.FILTER.getUpdate();
    }

    private String openSortSection() {

        /*TODO: load sorts*/

        return SQLQuerySection.SORT.getUpdate();
    }

}
