package com.tflow.model.editor.datasource;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.RoomType;
import com.tflow.model.editor.view.PropertyView;
import javafx.beans.property.Property;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataSourceSelector extends DataSource implements Selectable, HasEvent {

    private int dataSourceId;
    private LinePlug startPlug;

    private Step owner;

    private EventManager eventManager;

    /*for ProjectMapper*/
    public DataSourceSelector() {
        init();
    }

    public DataSourceSelector(String name, DataSourceType dataSourceType, String startPlug) {
        this.name = name;
        this.type = dataSourceType;
        this.startPlug = new StartPlug(startPlug);
        init();
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                LoggerFactory.getLogger(DataSourceSelector.class).debug("PROPERTY_CHANGED: property={}", property);
                if (PropertyVar.dataSourceId.equals(property.getVar())) {
                    Integer dataSourceId = (Integer) property.getNewValue();
                    String dataSourceName = getDataSourceName(dataSourceId);
                    LoggerFactory.getLogger(DataSourceSelector.class).debug("PROPERTY_CHANGED: dataSourceId={}, dataSourceName={}", dataSourceId, dataSourceName);
                    setName(dataSourceName);
                }
            }
        });
    }

    private String getDataSourceName(int dataSourceId) {
        Project project = owner.getOwner();
        String name = this.name;
        if (dataSourceId <= 0) return name;

        switch (type) {
            case DATABASE:
                Map<Integer, Database> databaseMap = project.getDatabaseMap();
                LoggerFactory.getLogger(DataSourceSelector.class).debug("databaseMap={}", databaseMap);
                Database database = databaseMap.get(dataSourceId);
                name = database.getName();
                break;

            case SFTP:
                SFTP sftp = project.getSftpMap().get(dataSourceId);
                name = sftp.getName();
                break;

            case LOCAL:
                Local local = project.getLocalMap().get(dataSourceId);
                name = local.getName();
                break;
        }

        return name;
    }

    private void init() {
        this.dataSourceId = -1;
        this.setRoomType(RoomType.DATA_SOURCE);
        this.image = "local.png";
        eventManager = new EventManager(this);
    }

    public void createPlugListeners() {
        startPlug.createDefaultPlugListener();
        createEventHandlers();
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Override
    public Properties getProperties() {
        return Properties.STEP_DATA_SOURCE;
    }

    @Override
    public String getSelectableId() {
        return "ds" + id;
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

    public Step getOwner() {
        return owner;
    }

    public void setOwner(Step owner) {
        this.owner = owner;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }
}
