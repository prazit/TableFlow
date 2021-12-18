package com.tflow.model.editor.datasource;

import java.io.Serializable;

public class DataSource implements Serializable {
    private static final long serialVersionUID = 2021121709996660010L;

    private String type;
    private String name;

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

    public String getPlug() {
        return plug;
    }

    public void setPlug(String plug) {
        this.plug = plug;
    }
}
