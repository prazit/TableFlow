package com.tflow.model.editor.datasource;

import com.tflow.model.editor.room.Room;

import java.io.Serializable;

public class DataSource extends Room implements Serializable {
    private static final long serialVersionUID = 2021121709996660010L;

    private String type;
    private String name;
    private String image;
    private String plug;

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
