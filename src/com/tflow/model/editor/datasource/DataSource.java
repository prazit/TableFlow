package com.tflow.model.editor.datasource;

import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.room.Room;

import java.io.Serializable;

public class DataSource extends Room implements Serializable {

    protected int id;
    protected DataSourceType type;
    protected String name;
    protected String image;
    protected LinePlug plug;

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

    public LinePlug getPlug() {
        return plug;
    }

    public void setPlug(LinePlug plug) {
        this.plug = plug;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", plug:" + plug +
                '}';
    }
}
