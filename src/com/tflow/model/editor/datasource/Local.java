package com.tflow.model.editor.datasource;

import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Local extends DataSource implements Selectable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Local local = (Local) o;
        return Objects.equals(rootPath, local.rootPath);
    }

    @Override
    public Properties getProperties() {
        return Properties.LOCAL_FILE;
    }

    @Override
    public String getSelectableId() {
        return "local" + id;
    }
}
