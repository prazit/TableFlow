package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataTable extends Room implements Serializable, Selectable, HasDataFile {
    private static final long serialVersionUID = 2021121709996660030L;

    private int id;
    private String name;
    private int index;
    private DataFile dataFile;
    private String query;
    private String idColName;
    private List<DataColumn> columnList;
    private List<DataOutput> outputList;

    /*noTransform can use Auto Generated Value*/
    private boolean noTransform;

    private String endPlug;
    private String startPlug;

    public DataTable(int id, String name, DataFile dataFile, String query, String idColName, boolean noTransform, String endPlug, String startPlug) {
        this.id = id;
        this.name = name;
        this.index = -1;
        this.dataFile = dataFile;
        if(dataFile != null) {
            dataFile.setOwner(this);
        }
        this.query = query;
        this.idColName = idColName;
        this.noTransform = noTransform;
        this.endPlug = endPlug;
        this.startPlug = startPlug;
        this.columnList = new ArrayList<>();
        this.outputList = new ArrayList<>();
        this.setRoomType(RoomType.DATA_TABLE);
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    @Override
    public boolean isDataTable() {
        return true;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getIdColName() {
        return idColName;
    }

    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }

    public List<DataColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<DataColumn> columnList) {
        this.columnList = columnList;
    }

    public List<DataOutput> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<DataOutput> outputList) {
        this.outputList = outputList;
    }

    public boolean isNoTransform() {
        return noTransform;
    }

    public void setNoTransform(boolean noTransform) {
        this.noTransform = noTransform;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_TABLE;
    }

    @Override
    public String getSelectableId() {
        return "dt" + id;
    }
}
