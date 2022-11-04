package com.tflow.model.editor.sql;

import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.room.Room;

import java.util.List;

public class QueryTable extends Room {

    private int id;
    private String name;

    private List<QueryColumn> columnList;

    private LinePlug startPlug;
    private LinePlug endPlug;

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

    public LinePlug getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    public LinePlug getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }
}
