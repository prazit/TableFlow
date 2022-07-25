package com.tflow.model.editor.room;

import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.util.DataTableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tower {

    private transient Logger log = LoggerFactory.getLogger(Tower.class);

    private int id;
    private List<Floor> floorList;
    private int roomsOnAFloor;

    private Step owner;

    /*for StepMapper*/
    public Tower() {
        init();
    }

    /*for StepMapper*/
    public Tower(int id) {
        this.id = id;
        init();
    }

    public Tower(int id, int roomsOnAFloor, Step owner) {
        this.id = id;
        this.roomsOnAFloor = roomsOnAFloor;
        this.owner = owner;
        init();
    }

    private void init() {
        floorList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void addRoom(int count) {
        for (Floor floor : floorList) {
            addRoom(count, floor);
        }
    }

    /**
     * @param count number of rooms to add
     * @param floor if null means add rooms to all floors in this tower, otherwise add rooms to the specified floor.
     */
    public void addRoom(int count, Floor floor) {
        List<Floor> floorList;
        if (floor == null) {
            floorList = this.floorList;
        } else {
            floorList = Collections.singletonList(floor);
        }

        Floor firstFloor = floorList.get(0);
        Project project = firstFloor.getTower().getOwner().getOwner();
        for (Floor fl : floorList) {
            List<Room> roomList = fl.getRoomList();
            int roomIndex = roomList.size();
            for (int c = 0; c < count; c++) {
                EmptyRoom emptyRoom = new EmptyRoom(roomIndex, floor, DataTableUtil.newElementId(project));
                roomList.add(emptyRoom);
                roomIndex++;
            }
        }
        roomsOnAFloor = firstFloor.getRoomList().size();
    }

    public void addFloor(int count) {
        int flIndex = floorList.size();
        for (int c = 0; c < count; c++) {
            Floor floor = new Floor(DataTableUtil.newUniqueId(owner.getOwner()), flIndex++, this);
            floorList.add(floor);
            addRoom(roomsOnAFloor, floor);
        }
    }

    public Floor getFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= floorList.size()) return null;

        return floorList.get(floorIndex);
    }

    public Floor getAvailableFloor(int roomIndex, boolean newFloor) {
        return getAvailableFloor(roomIndex, newFloor, -1);
    }

    public Floor getAvailableFloor(int roomIndex, boolean newFloor, int newFloorIndex) {
        Floor floor = null;
        if (!newFloor && floorList.size() > 0) {
            for (Floor fl : floorList) {
                if (fl.isEmpty(roomIndex)) {
                    floor = fl;
                    break;
                }
            }
        }

        if (floor == null) {
            int flIndex = newFloorIndex < 0 ? floorList.size() : newFloorIndex;
            floor = new Floor(DataTableUtil.newUniqueId(owner.getOwner()), flIndex, this);
            floorList.add(flIndex, floor);
            addRoom(roomsOnAFloor, floor);
        }

        return floor;
    }

    /**
     * Stack of rooms on every floor with the same index.
     *
     * @param roomIndex start at 0.
     */
    public List<Room> getStack(int roomIndex) {
        List<Room> roomList = new ArrayList<>();
        for (Floor floor : floorList) {
            roomList.add(floor.getRoomList().get(roomIndex));
        }
        return roomList;
    }

    public Step getOwner() {
        return owner;
    }

    public void setOwner(Step owner) {
        this.owner = owner;
    }

    public boolean isEmpty() {
        return floorList.isEmpty();
    }

    public int getRoomsOnAFloor() {
        return roomsOnAFloor;
    }

    public void setRoomsOnAFloor(int roomsOnAFloor) {
        this.roomsOnAFloor = roomsOnAFloor;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", floorList:" + Arrays.toString(floorList.toArray()) +
                ", roomsOnAFloor:" + roomsOnAFloor +
                '}';
    }

    public void setRoom(int floorIndex, int roomIndex, Room roomer) {
        if (floorIndex < 0) {
            log.error("Invalid floorIndex : Tower[id:{}].setRoom(floorIndex:{}, floorCount:{}, roomIndex:{}, roomer:{})", id, floorIndex, floorList.size(), roomIndex, roomer.getRoomType());
            return;
        } else if (floorIndex >= floorList.size()) {
            addFloor(floorIndex - floorList.size() + 1);
        }

        Floor floor = floorList.get(floorIndex);
        floor.setRoom(roomIndex, roomer);
    }
}
