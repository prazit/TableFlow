package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class ColumnFx implements Serializable, Selectable, HasEndPlug {
    private static final long serialVersionUID = 2021121709996660042L;

    private int id;
    private String name;
    private ColumnFunction function;
    private Map<String, Object> propertyMap;

    private List<ColumnFxPlug> endPlugList;
    private LinePlug startPlug;

    private DataColumn owner;

    public ColumnFx(DataColumn owner, ColumnFunction function, String name, String startPlug, String endPlug) {
        this.name = name;
        this.function = function;
        this.startPlug = new StartPlug(startPlug);
        this.owner = owner;
        propertyMap = new HashMap<>();
        function.getProperties().initPropertyMap(propertyMap);
        endPlugList = createEndPlugList(endPlug);
    }

    private List<ColumnFxPlug> createEndPlugList(String endPlug) {
        List<ColumnFxPlug> plugList = new ArrayList<>();

        String endPlugId;
        for (PropertyView propertyView : function.getProperties().getPlugPropertyList()) {
            endPlugId = propertyView.getVar().equals("sourceColumn") ? endPlug : "";
            plugList.add(new ColumnFxPlug(endPlugId, propertyView.getLabel()));
        }

        LoggerFactory.getLogger(getClass()).warn("createEndPlugList:plugList={}", Arrays.toString(plugList.toArray()));
        return plugList;
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

    public ColumnFunction getFunction() {
        return function;
    }

    public void setFunction(ColumnFunction function) {
        this.function = function;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public List<ColumnFxPlug> getEndPlugList() {
        return endPlugList;
    }

    public void setEndPlugList(List<ColumnFxPlug> endPlugList) {
        this.endPlugList = endPlugList;
    }

    @Override
    public LinePlug getEndPlug() {
        return endPlugList.size() > 0 ? endPlugList.get(0) : new LinePlug("");
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        /*nothing*/
    }

    @Override
    public LinePlug getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    public DataColumn getOwner() {
        return owner;
    }

    public void setOwner(DataColumn owner) {
        this.owner = owner;
    }

    @Override
    public Properties getProperties() {
        return function.getProperties();
    }

    @Override
    public String getSelectableId() {
        return "cfx" + id;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", function:'" + function + '\'' +
                ", startPlug:" + startPlug +
                ", propertyMap:" + propertyMap +
                ", endPlugList:" + Arrays.toString(endPlugList.toArray()) +
                '}';
    }
}
