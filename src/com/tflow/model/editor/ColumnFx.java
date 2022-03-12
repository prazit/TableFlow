package com.tflow.model.editor;

import com.tflow.HasEvent;
import com.tflow.model.editor.view.PropertyView;

import java.io.Serializable;
import java.util.*;

/**
 * <b>Event</b><br/>
 * <li>REMOVE</li> occurs when the Remove-Button on startPlug is clicked.
 * <li>PROPERTY_CHANGED</li>  occurs after the value of property is changed.
 */
public class ColumnFx implements Serializable, Selectable, HasEndPlug, HasEvent {
    private static final long serialVersionUID = 2021121709996660042L;

    private int id;
    private String name;
    private ColumnFunction function;
    private Map<String, Object> propertyMap;

    private List<ColumnFxPlug> endPlugList;
    private LinePlug startPlug;

    private DataColumn owner;

    private EventManager eventManager;

    public ColumnFx(ColumnFunction function, String name, String startPlug, DataColumn owner) {
        this.name = name;
        this.function = function;
        this.startPlug = createStartPlug(startPlug);
        this.owner = owner;
        eventManager = new EventManager(this);
        propertyMap = new HashMap<>();
        endPlugList = new ArrayList<>();
        createEndPlugList();
        function.getProperties().initPropertyMap(propertyMap);
    }

    private StartPlug createStartPlug(String plugId) {
        StartPlug startPlug = new StartPlug(plugId);
        startPlug.setExtractButton(true);

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

        return startPlug;
    }

    /**
     * Need to re-create endPlugList again after the function is changed.
     */
    private void createEndPlugList() {
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
            ColumnFxPlug columnFxPlug = new ColumnFxPlug(project.newUniqueId(), propertyView.getType().getDataType(), propertyView.getLabel(), endPlugId, this);
            endPlugList.add(columnFxPlug);
            /*need to update selectableMap for each*/
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

    public void setFunction(ColumnFunction function) {
        if(this.function == function) return;
        this.function = function;
        createEndPlugList();
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
    public EventManager getEventManager() {
        return eventManager;
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
