package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColumnFxTable extends Room {

    private TransformTable ownerTable;
    private List<ColumnFx> columnFxList;

    public ColumnFxTable(TransformTable ownerTable) {
        this.ownerTable = ownerTable;
        columnFxList = new ArrayList<>();
        this.setRoomType(RoomType.COLUMN_FX_TABLE);
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

    @Override
    public String toString() {
        return "{" +
                "ownerTable:'" + ownerTable.getSelectableId() + "'" +
                ", columnFxList:" + Arrays.toString(columnFxList.toArray()) +
                '}';
    }
}
