package com.tflow.model.editor.datasource;

import com.tflow.model.editor.room.Room;

import java.io.Serializable;

public class DataSource extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660010L;

    protected int id;
    protected DataSourceType type;
    protected String name;
    protected String image;
    protected String plug;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPlug() {
        return plug;
    }

    public void setPlug(String plug) {
        this.plug = plug;
    }
}
