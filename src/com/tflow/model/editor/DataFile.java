package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;

import java.io.Serializable;
import java.util.Map;

public class DataFile extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660020L;

    private DataFileType type;
    private String image;
    private String name;
    private Map<String, String> paramMap;

    private String endPlug;
    private String startPlug;

    public DataFile(DataFileType type, String name, Map<String, String> paramMap, String endPlug, String startPlug) {
        this.type = type;
        this.name = name;
        this.image = type.getImage();
        this.paramMap = paramMap;
        this.endPlug = endPlug;
        this.startPlug = startPlug;
        this.setRoomType(RoomType.DATA_FILE);
    }

    public DataFileType getType() {
        return type;
    }

    public void setType(DataFileType type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

}
