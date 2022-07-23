package com.tflow.model.editor;

import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformTable extends DataTable {

    private SourceType sourceType;
    private String sourceSelectableId;
    private List<TableFx> fxList;
    private ColumnFxTable columnFxTable;

    /*for ProjectMapper*/
    public TransformTable() {
        init();
    }

    public TransformTable(String name, String sourceSelectableId, SourceType sourceType, String idColName, String endPlug, String startPlug, Step owner) {
        super(name, null,  idColName,  endPlug, startPlug, owner);
        this.sourceSelectableId = sourceSelectableId;
        this.sourceType = sourceType;
        init();
    }

    private void init() {
        setRoomType(RoomType.TRANSFORM_TABLE);
        fxList = new ArrayList<>();
        columnFxTable = new ColumnFxTable(this);
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceSelectableId() {
        return sourceSelectableId;
    }

    public void setSourceSelectableId(String sourceSelectableId) {
        this.sourceSelectableId = sourceSelectableId;
    }

    public List<TableFx> getFxList() {
        return fxList;
    }

    public void setFxList(List<TableFx> fxList) {
        this.fxList = fxList;
    }

    public ColumnFxTable getColumnFxTable() {
        return columnFxTable;
    }

    public void setColumnFxTable(ColumnFxTable columnFxTable) {
        this.columnFxTable = columnFxTable;
    }

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_TABLE;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", index:" + index +
                ", level:" + level +
                ", idColName:'" + idColName + '\'' +
                ", noTransform:" + noTransform +
                ", endPlug:" + endPlug +
                ", startPlug:" + startPlug +
                ", connectionCount:" + connectionCount +
                ", columnList:" + Arrays.toString(columnList.toArray()) +
                ", outputList:" + Arrays.toString(outputList.toArray()) +
                ", sourceType:" + sourceType +
                ", sourceSelectableId:'" + sourceSelectableId + '\'' +
                ", fxList:" + Arrays.toString(fxList.toArray()) +
                ", columnFxTable:" + columnFxTable +
                '}';
    }
}
