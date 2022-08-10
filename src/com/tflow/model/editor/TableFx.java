package com.tflow.model.editor;

import com.tflow.model.data.IDPrefix;

import java.util.HashMap;
import java.util.Map;

public class TableFx implements Selectable {

    private int id;
    private String name;

    private boolean useFunction;
    private TableFunction function;
    private Map<String, Object> propertyMap;
    private String propertyOrder;

    private TransformTable owner;

    /*for projectMapper*/
    public TableFx() {
        /*nothing*/
    }

    public TableFx(TableFunction function, String name, TransformTable owner) {
        this.name = name;
        this.owner = owner;
        propertyMap = new HashMap<>();
        this.function = function;
        propertyOrder = function.getProperties().initPropertyMap(propertyMap);
    }

    /*for projectMapper*/
    public TableFx(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseFunction() {
        return useFunction;
    }

    public void setUseFunction(boolean useFunction) {
        this.useFunction = useFunction;
    }

    public TableFunction getFunction() {
        return function;
    }

    public void setFunction(TableFunction function) {
        this.function = function;
        propertyOrder = function.getProperties().initPropertyMap(propertyMap);
    }

    public String getPropertyOrder() {
        return propertyOrder;
    }

    public void setPropertyOrder(String propertyOrder) {
        this.propertyOrder = propertyOrder;
    }

    public TransformTable getOwner() {
        return owner;
    }

    public void setOwner(TransformTable owner) {
        this.owner = owner;
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    @Override
    public Properties getProperties() {
        return function.getProperties();
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.TRANSFORMATION.getPrefix() + id;
    }
}
