package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;

import java.util.*;

public class ColumnFx implements Selectable, HasEndPlug {

    private int id;
    private String name;
    private ColumnFunction function;
    private Map<String, Object> propertyMap;

    private List<ColumnFxPlug> endPlugList;
    private LinePlug startPlug;

    private DataColumn owner;

    /*for projectMapper*/
    public ColumnFx() {
        /*nothing*/
    }

    public ColumnFx(ColumnFunction function, String name, String startPlug, DataColumn owner) {
        this.name = name;
        this.function = function;
        createStartPlug(startPlug);
        this.owner = owner;
        propertyMap = new HashMap<>();
        endPlugList = new ArrayList<>();
        createEndPlugList();
        function.getProperties().initPropertyMap(propertyMap);
    }

    /*for projectMapper*/
    public ColumnFx(int id) {
        this.id = id;
    }

    private void createStartPlug(String plugId) {
        startPlug = new StartPlug(plugId);
        startPlug.setExtractButton(true);

        createStartPlugListener();
    }

    private void createStartPlugListener() {
        startPlug.setListener(new PlugListener(startPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
            }

            @Override
            public void unplugged(Line line) {
                plug.setPlugged(false);
                plug.setRemoveButton(false);
            }
        });
    }

    /*call after projectMapper*/
    public void createPlugListeners() {
        createStartPlugListener();
        for (ColumnFxPlug columnFxPlug : endPlugList) {
            columnFxPlug.createDefaultPlugListener();
        }
    }

    /**
     * Need to re-create endPlugList again after the function is changed.
     */
    public void createEndPlugList() {
        Step step = owner.getOwner().getOwner();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        if (endPlugList.size() > 0) {
            /*need to remove old list from selectableMap before reset the list*/
            for (ColumnFxPlug columnFxPlug : endPlugList) {
                selectableMap.remove(columnFxPlug.getSelectableId());
                step.removeLine(columnFxPlug.getLine());
            }
            endPlugList.clear();
        }

        String endPlugId;
        for (PropertyView propertyView : function.getProperties().getPlugPropertyList()) {
            endPlugId = project.newElementId();
            /*Notice: columnFxPlug use defaultPlugListener*/
            ColumnFxPlug columnFxPlug = new ColumnFxPlug(project.newUniqueId(), propertyView.getType().getDataType(), propertyView.getLabel(), endPlugId, this);
            endPlugList.add(columnFxPlug);
            /*update selectableMap for each*/
            selectableMap.put(columnFxPlug.getSelectableId(), columnFxPlug);
        }
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

    /*TODO: change Function like this need the Action to update related data to server*/
    public void setFunction(ColumnFunction function) {
        boolean genEndPlugList = this.function != function && this.function != null;
        this.function = function;
        if (genEndPlugList) createEndPlugList();
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
