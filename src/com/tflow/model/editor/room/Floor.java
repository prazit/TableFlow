package com.tflow.model.editor.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
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
        roomList.add(roomIndex, room);
    }

    boolean isAvailableFloor() {
        int emptyCount = 0;
        for (Room room : roomList) {
            if (room.isEmptyRoom()) {
                emptyCount++;
            }
        }
        return emptyCount == roomList.size();
    }
}
