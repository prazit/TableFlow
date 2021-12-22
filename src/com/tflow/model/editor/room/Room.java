package com.tflow.model.editor.room;

import com.tflow.model.editor.ColumnFx;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.DataTable;
import com.tflow.model.editor.TransformTable;
import com.tflow.model.editor.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Known implementation classes.<br/>
 * EmptyRoom, DataSource, DataFile, DataTable, ColumnFx, TransformTable
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 2021121909996660030L;

    private Logger log = LoggerFactory.getLogger(Room.class);

    private String elementId;
    private int roomIndex;
    private RoomType roomType;
    private Floor floor;

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

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
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

    @Override
    public String toString() {
        return "Room{" +
                "elementId='" + elementId + '\'' +
                ", roomIndex=" + roomIndex +
                ", floor=" + floor +
                ", instanceOf=" + this.getClass().getName() +
                '}';
    }
}
