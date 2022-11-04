package com.tflow.model.editor.sql;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.room.Tower;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query implements Selectable {

    private int id;
    private String name;

    private Tower tower;

    private List<QueryTable> tableList;
    private List<Line> lineList;

    private List<QueryFilter> filterList;
    private List<QuerySort> sortList;

    public Query() {
        name= "";
        tower = new Tower();
        tableList = new ArrayList<>();
        lineList = new ArrayList<>();
        filterList = new ArrayList<>();
        sortList = new ArrayList<>();
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

    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }
}
