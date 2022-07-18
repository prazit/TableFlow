package com.tflow.model.editor.room;

public class EmptyRoom extends Room {

    public EmptyRoom(int roomIndex, Floor floor) {
        setRoomIndex(roomIndex);
        setFloor(floor);
        setRoomType(RoomType.EMPTY);
    }

    public EmptyRoom(int roomIndex, Floor floor, String elementId) {
        setRoomIndex(roomIndex);
        setFloor(floor);
        setElementId(elementId);
        setRoomType(RoomType.EMPTY);
    }

}
