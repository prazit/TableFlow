package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.view.PropertyView;

import java.util.Map;

public class Variable implements Selectable, HasEvent {

    private VariableType type;
    private int index;

    private int id;
    private String name;
    private String value;
    private String description;

    private EventManager eventManager;

    public Variable(int index, VariableType type, String name, String description) {
        this.type = type;
        this.index = index;
        this.name = name;
        this.description = description;
        this.value = "";
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.name.equals(property.getVar())) {
                    eventManager.fireEvent(EventName.NAME_CHANGED, property);
                }
            }
        });
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public VariableType getType() {
        return type;
    }

    public void setType(VariableType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.VARIABLE;
    }

    @Override
    public Properties getProperties() {
        if (VariableType.SYSTEM == type) return Properties.SYSTEM_VARIABLE;
        return Properties.USER_VARIABLE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.VARIABLE.getPrefix() + (id > 0 ? id : index);
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String toString() {
        return "{" +
                "type:" + type +
                ", index:" + index +
                ", id:" + id +
                ", name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", description:'" + description + '\'' +
                '}';
    }
}
