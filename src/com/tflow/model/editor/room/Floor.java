package com.tflow.model.editor.room;

import com.tflow.model.editor.Selectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Floor implements Serializable {
    private static final long serialVersionUID = 2021121909996660020L;

    private Logger log = LoggerFactory.getLogger(Floor.class);

    private int index;
    private Tower tower;
    private List<Room> roomList;

    public Floor(int index, Tower owner) {
        this.index = index;
        this.tower = owner;
        roomList = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Tower getTower() {
        return tower;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
    }

    /**
     * Don't use this function, for serialization only.
     * Keep in 'Public' accessible for XHTML.
     ***/
    public List<Room> getRoomList() {
        return roomList;
    }

    /**
     * Don't use this function, for serialization only.
     ***/
    void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public void setRoom(int roomIndex, Room room) {
        if (roomIndex < 0 || roomIndex >= roomList.size()) {
            log.warn("Invalid roomIndex : Floor[{}].setRoom(roomIndex:{}, room)", index, roomIndex);
            return;
        }

        Room old = roomList.remove(roomIndex);
        old.setRoomIndex(0);

        room.setRoomIndex(roomIndex);
        room.setFloor(this);
        roomList.add(roomIndex, room);
    }

    public boolean isEmpty() {
        return isEmpty(-1);
    }

    public boolean isEmpty(int roomIndex) {
        int emptyCount = 0;
        for (Room room : roomList) {
            if (room.getRoomType() == RoomType.EMPTY) {
                if (roomIndex == room.getRoomIndex()) {
                    return true;
                }
                emptyCount++;
            }
        }
        return emptyCount == roomList.size();
    }

    public List<Selectable> getSelectableList() {
        List<Selectable> selectableList = new ArrayList<>();
        for (Room room : roomList) {
            if (room instanceof Selectable) {
                selectableList.add((Selectable) room);
            }
        }
        return selectableList;
    }

    @Override
    public String toString() {
        return "{" +
                "index:" + index +
                ", roomList:" + Arrays.toString(roomList.toArray()) +
                '}';
    }
}
