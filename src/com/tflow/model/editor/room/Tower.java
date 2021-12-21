package com.tflow.model.editor.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tower implements Serializable {
    private static final long serialVersionUID = 2021121909996660010L;

    private Logger log = LoggerFactory.getLogger(Tower.class);

    private List<Floor> floorList;
    private int roomsOnAFloor;
    private Room activeRoom;

    public Tower(int roomsOnAFloor) {
        this.roomsOnAFloor = roomsOnAFloor;
        floorList = new ArrayList<>();
    }

    /**
     * serialization only
     ***/
    public List<Floor> getFloorList() {
        return floorList;
    }

    /**
     * serialization only
     ***/
    public void setFloorList(List<Floor> floorList) {
        this.floorList = floorList;
    }

    /**
     * @param floor if null means add rooms to all floors in this tower, otherwise add rooms to the specified floor.
     */
    public void addRoom(int count, Floor floor) {
        List<Floor> floorList;
        if (floor == null) {
            floorList = this.floorList;
        } else {
            floorList = Arrays.asList(floor);
        }

        EmptyRoom emptyRoom = new EmptyRoom();
        for (Floor fl : floorList) {
            List<Room> roomList = fl.getRoomList();
            for (int r = 0; r < count; r++) {
                roomList.add(r, emptyRoom);
            }
        }
    }

    public Floor getFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= floorList.size()) return null;

        return floorList.get(floorIndex);
    }

    public Floor getAvailableFloor(boolean newFloor) {
        Floor floor = null;
        if (!newFloor && floorList.size() > 0) {
            for (Floor fl : floorList) {
                if (fl.isAvailableFloor()) {
                    floor = fl;
                    break;
                }
            }
        }

        if (floor == null) {
            int flIndex = floorList.size();
            floor = new Floor(flIndex, this);
            addRoom(roomsOnAFloor, floor);
            floorList.add(floor);
        }

        return floor;
    }

    public Room getActiveRoom() {
        return activeRoom;
    }

    public void setActiveRoom(Room activeRoom) {
        this.activeRoom = activeRoom;
    }
}
