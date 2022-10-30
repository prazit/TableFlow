package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataColumn implements Serializable, Selectable {

    protected int id;
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
        createStartPlug(startPlug);
        this.owner = owner;
    }

    private void createStartPlug(String stringPlug) {
        this.startPlug = new StartPlug(stringPlug);
        createStartPlugListener();
    }

    /*call after projectMapper*/
    public void createPlugListeners() {
        createStartPlugListener();
    }

    private void createStartPlugListener() {
        startPlug.setListener(new PlugListener(startPlug) {
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
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.DATA_COLUMN;
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
        return IDPrefix.DATA_COLUMN.getPrefix() + id;
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
                "id:" + id +
                ", index:" + index +
                ", name:'" + name + '\'' +
                ", type:'" + type + '\'' +
                ", startPlug:'" + startPlug + '\'' +
                ", selectableId:'" + getSelectableId() + '\'' +
                '}';
    }
}
