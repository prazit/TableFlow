package com.tflow.model.editor;

import com.tflow.HasEvent;

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
        endPlugList = new ArrayList<>();
        eventManager = new EventManager();
        propertyMap = new HashMap<>();
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
                eventManager.fireEvent(EventName.REMOVE);
            }
        });

        return startPlug;
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
