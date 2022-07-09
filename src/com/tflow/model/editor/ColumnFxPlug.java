package com.tflow.model.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ColumnFxPlug extends LinePlug implements Serializable, Selectable, HasEndPlug {
    private static final long serialVersionUID = 2021121709996660045L;

    private int id;
    private DataType type;
    private String name;
    private ColumnFx owner;

    /*for ProjectMapper*/
    public ColumnFxPlug() {
        /*nothing*/
    }

    public ColumnFxPlug(int id, DataType type, String name, String plugId, ColumnFx owner) {
        super(plugId);
        this.id = id;
        this.type = type;
        this.name = name;
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public ColumnFx getOwner() {
        return owner;
    }

    public void setOwner(ColumnFx owner) {
        this.owner = owner;
    }

    @Override
    public Properties getProperties() {
        return Properties.FX_PARAM;
    }

    @Override
    public String getSelectableId() {
        return "cfxp" + id;
    }

    @Override
    public LinePlug getStartPlug() {
        return this;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public LinePlug getEndPlug() {
        return this;
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        /*nothing*/
    }
}
