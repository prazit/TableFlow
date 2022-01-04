package com.tflow.model.editor.datasource;

import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.List;

public class Local extends DataSource {
    private static final long serialVersionUID = 2021121709996660013L;

    private List<String> pathHistory;
    private String rootPath;

    public Local(String name, String rootPath, String plug) {
        setName(name);
        setType(DataSourceType.LOCAL);
        setImage("Local.png");
        setPlug(plug);
        this.rootPath = rootPath;
        pathHistory = new ArrayList<>();
        this.setRoomType(RoomType.DATA_SOURCE);
    }

    public List<String> getPathHistory() {
        return pathHistory;
    }

    public void setPathHistory(List<String> pathHistory) {
        this.pathHistory = pathHistory;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
