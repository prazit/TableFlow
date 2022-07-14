package com.tflow.model.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataColumn implements Serializable, Selectable {

    private int id;
    protected int index;
    protected DataType type;
    protected String name;

    protected LinePlug startPlug;

    protected DataTable owner;

    /*for projectMapper*/
    public DataColumn() {
        /*nothing*/
    }

    public DataColumn(int index, DataType type, String name, String startPlug, DataTable owner) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.startPlug = createStartPlug(startPlug);
        this.owner = owner;
    }

    private LinePlug createStartPlug(String stringPlug) {
        LinePlug plug = new StartPlug(stringPlug);
        plug.setListener(new PlugListener(plug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
                owner.connectionCreated();
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                plug.setRemoveButton(plugged);
                owner.connectionRemoved();
            }
        });
        return plug;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LinePlug getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_COLUMN;
    }

    @Override
    public String getSelectableId() {
        return "dc" + id;
    }

    public DataTable getOwner() {
        return owner;
    }

    public void setOwner(DataTable owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "{" +
                "index:" + index +
                ", selectableId:'" + ((Selectable) this).getSelectableId() + '\'' +
                ", type:'" + type + '\'' +
                ", name:'" + name + '\'' +
                ", startPlug:'" + startPlug + '\'' +
                '}';
    }
}
