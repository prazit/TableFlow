package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ColumnFxTable extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660044L;

    private TransformTable ownerTable;
    private List<ColumnFx> columnFxList;

    public ColumnFxTable(TransformTable ownerTable) {
        this.ownerTable = ownerTable;
        columnFxList = new ArrayList<>();
    }

    public List<ColumnFx> getColumnFxList() {
        return columnFxList;
    }

    public void setColumnFxList(List<ColumnFx> columnFxList) {
        this.columnFxList = columnFxList;
    }

    public TransformTable getOwnerTable() {
        return ownerTable;
    }

    public void setOwnerTable(TransformTable ownerTable) {
        this.ownerTable = ownerTable;
    }
}
