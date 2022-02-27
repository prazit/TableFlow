package com.tflow.model.editor;

import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.List;

public class TransformTable extends DataTable {
    private static final long serialVersionUID = 2021121709996660040L;

    private SourceType sourceType;
    private String sourceSelectableId;
    private List<TableFx> fxList;
    private ColumnFxTable columnFxTable;

    public TransformTable(String name, String sourceSelectableId, SourceType sourceType, String idColName, String endPlug, String startPlug, Step owner) {
        super(name, null,  idColName,  endPlug, startPlug, owner);
        this.sourceSelectableId = sourceSelectableId;
        this.sourceType = sourceType;
        fxList = new ArrayList<>();
        columnFxTable = new ColumnFxTable(this);
        this.setRoomType(RoomType.TRANSFORM_TABLE);
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
}
