package com.tflow.model.editor.room;

import com.tflow.model.editor.ColumnFx;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.DataTable;
import com.tflow.model.editor.TransformTable;
import com.tflow.model.editor.datasource.DataSource;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 2021121909996660030L;

    private String elementId;
    private int roomIndex;
    private String floor;

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public int getRoomIndex() {
        return roomIndex;
    }

    public void setRoomIndex(int roomIndex) {
        this.roomIndex = roomIndex;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public boolean isDataSource() {
        return this instanceof DataSource;
    }

    public boolean isDataFile() {
        return this instanceof DataFile;
    }

    public boolean isDataTable() {
        return this instanceof DataTable;
    }

    public boolean isColumnFx() {
        return this instanceof ColumnFx;
    }

    public boolean isTransformTable() {
        return this instanceof TransformTable;
    }

    public boolean isEmptyRoom() {
        return this instanceof EmptyRoom;
    }
}
