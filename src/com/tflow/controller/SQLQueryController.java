package com.tflow.controller;

import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.JavaScript;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.sql.Query;
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

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    @Override
    public Page getPage() {
        return workspace.getCurrentPage();
    }

    @Override
    void onCreation() {
        dataFile = (DataFile) workspace.getProject().getActiveStep().getActiveObject();
        reloadTableList();
        reloadQuery();
        selectQuery();
    }

    private void reloadTableList() {
        /*TODO: load table list from DBMS using DConvers*/
    }

    private void reloadQuery() {
        int queryId = (Integer) dataFile.getPropertyMap().get(PropertyVar.queryId.name());
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
        jsBuilder.pre(JavaScript.selectObject, query.getSelectableId()).runOnClient();
    }

    public void openSection(TabChangeEvent event) {
        log.debug("openSection:fromClient");
        /*TODO: openSection on SQLQuery*/
    }

    public void openQueryCondition(TabChangeEvent event) {
        log.debug("openQueryCondition:fromClient");
        /*TODO: openQueryCondition*/
    }
}
