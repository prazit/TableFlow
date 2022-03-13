package com.tflow.model.editor.room;

import com.tflow.model.editor.Selectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Known implementation classes.<br/>
 * EmptyRoom, DataSource, DataFile, DataTable, ColumnFx, TransformTable
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 2021121909996660030L;

    private transient Logger log = LoggerFactory.getLogger(Room.class);

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

    @Override
    public String toString() {
        return "{" +
                (this instanceof Selectable ? "selectableId:'" + ((Selectable) this).getSelectableId() + '\'' : "selectableId: false") +
                ", elementId:'" + elementId + '\'' +
                ", roomIndex:" + roomIndex +
                ", roomType:" + roomType +
                '}';
    }
}
