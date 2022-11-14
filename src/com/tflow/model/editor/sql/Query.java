package com.tflow.model.editor.sql;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.room.Tower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Query implements Selectable {

    private int id;
    private String name;

    private Tower tower;

    private List<String> schemaList;

    /*selected column list include normal-column and alias-columns/compute-columns*/
    private List<QueryColumn> columnList;

    private List<QueryTable> tableList;
    private List<Line> lineList;

    private List<QueryFilter> filterList;
    private List<QuerySort> sortList;

    /*-- view only --*/
    private List<NameValue> quickColumnList;

    private DataFile owner;

    private List<String> allSchemaList;

    public Query() {
        name = "";
        tower = new Tower();
        columnList = new ArrayList<>();
        tableList = new ArrayList<>();
        lineList = new ArrayList<>();
        filterList = new ArrayList<>();
        sortList = new ArrayList<>();
        schemaList = new ArrayList<>();
        allSchemaList = new ArrayList<>();
    }

    public List<NameValue> getQuickColumnList() {
        return quickColumnList;
    }

    public void setQuickColumnList(List<NameValue> quickColumnList) {
        this.quickColumnList = quickColumnList;
    }

    public void refreshQuickColumnList() {
        if (columnList.size() == 0) return;

        quickColumnList = new ArrayList<>();
        int index = 0;
        for (QueryColumn column : columnList) {
            quickColumnList.add(new NameValue(column.getName(), column.getValue(), index++));
        }
        quickColumnList.get(columnList.size() - 1).setLast(true);
    }

    public Tower getTower() {
        return tower;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QueryColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<QueryColumn> columnList) {
        this.columnList = columnList;
    }

    public List<QueryTable> getTableList() {
        return tableList;
    }

    public void setTableList(List<QueryTable> tableList) {
        this.tableList = tableList;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public List<QueryFilter> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<QueryFilter> filterList) {
        this.filterList = filterList;
    }

    public List<QuerySort> getSortList() {
        return sortList;
    }

    public void setSortList(List<QuerySort> sortList) {
        this.sortList = sortList;
    }

    public DataFile getOwner() {
        return owner;
    }

    public void setOwner(DataFile owner) {
        this.owner = owner;
    }

    public List<String> getSchemaList() {
        return schemaList;
    }

    public void setSchemaList(List<String> schemaList) {
        this.schemaList = schemaList;
    }

    public List<String> getAllSchemaList() {
        return allSchemaList;
    }

    public void setAllSchemaList(List<String> allSchemaList) {
        this.allSchemaList = allSchemaList;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.QUERY;
    }

    @Override
    public Properties getProperties() {
        return Properties.QUERY;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.QUERY.getPrefix() + id;
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", columnList:" + Arrays.toString(columnList.toArray()) +
                ", tableList:" + Arrays.toString(tableList.toArray()) +
                ", filterList:" + Arrays.toString(filterList.toArray()) +
                ", sortList:" + Arrays.toString(sortList.toArray()) +
                ", schemaList:" + Arrays.toString(schemaList.toArray()) +
                ", tower:" + tower +
                ", lineList:" + Arrays.toString(lineList.toArray()) +
                '}';
    }
}
