package com.tflow.model.editor.room;

import com.tflow.model.editor.Selectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Known implementation classes.<br/>
 * EmptyRoom, DataSource, DataFile, DataTable, ColumnFx, TransformTable
 */
public class Room {
    private transient Logger log = LoggerFactory.getLogger(Room.class);

    private String elementId;
    private RoomType roomType;

    private int roomIndex;
    private int floorIndex;

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

    public int getFloorIndex() {
        return floorIndex;
    }

    public void setFloorIndex(int floorIndex) {
        this.floorIndex = floorIndex;
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
        if (floor != null) this.floorIndex = floor.getIndex();
    }

    public String toFloorString() {
        return "{" +
                (this instanceof Selectable ? "selectableId:'" + ((Selectable) this).getSelectableId() + '\'' : "selectableId: null") +
                ", elementId:'" + elementId + '\'' +
                ", floorIndex:" + roomIndex +
                ", roomIndex:" + roomIndex +
                ", roomType:" + roomType +
                ", class:'" + getClass().getName() + "'" +
                '}';
    }
}
