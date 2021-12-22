package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;

import java.io.Serializable;
import java.util.List;

public class TransformTable extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660040L;

    private String id;
    private String name;
    private int index;
    private DataTable dataTable;
    private String idColName;

    private List<TransformColumn> columnList;
    private List<TableFx> fxList;
    private List<DataOutput> outputList;

    private String endPlug;
    private String startPlug;

    public TransformTable(String id, String name, int index, DataTable dataTable, String idColName, String endPlug, String startPlug) {
        this.id = id;
        this.name = name;
        this.index = index;
        this.dataTable = dataTable;
        this.idColName = idColName;
        this.endPlug = endPlug;
        this.startPlug = startPlug;
        this.setRoomType(RoomType.TRANSFORM_TABLE);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public String getIdColName() {
        return idColName;
    }

    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }

    public List<TransformColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<TransformColumn> columnList) {
        this.columnList = columnList;
    }

    public List<TableFx> getFxList() {
        return fxList;
    }

    public void setFxList(List<TableFx> fxList) {
        this.fxList = fxList;
    }

    public List<DataOutput> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<DataOutput> outputList) {
        this.outputList = outputList;
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
}
