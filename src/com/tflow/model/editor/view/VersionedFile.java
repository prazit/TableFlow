package com.tflow.model.editor.view;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.Versioned;
import com.tflow.model.editor.*;

import java.util.Date;
import java.util.Map;

public class VersionedFile implements Selectable, HasEvent {

    private int index;

    private Versioned id;
    private String name;
    private Date uploadedDate;

    private EventManager eventManager;

    public VersionedFile() {
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Versioned getId() {
        return id;
    }

    public void setId(Versioned id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.VERSIONED;
    }

    @Override
    public Properties getProperties() {
        return Properties.VERSIONED;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.VERSIONED.getPrefix() + id.getFileId();
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
}
