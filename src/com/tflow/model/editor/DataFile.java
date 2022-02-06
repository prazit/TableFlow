package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataFile extends Room implements Serializable, Selectable, HasEndPlug {
    private static final long serialVersionUID = 2021121709996660020L;

    private Logger log = LoggerFactory.getLogger(DataFile.class);

    private int id;
    private DataSource dataSource;
    private DataFileType type;
    private String name;
    private String path;

    private Map<String, Object> propertyMap;

    private LinePlug endPlug;
    private LinePlug startPlug;

    private HasDataFile owner;

    public DataFile(DataSource dataSource, DataFileType type, String name, String path, String endPlug, String startPlug) {
        this.dataSource = dataSource;
        this.type = type;
        this.name = name;
        this.path = path;
        this.propertyMap = new HashMap<>();
        type.getProperties().initPropertyMap(propertyMap);
        this.endPlug = new EndPlug(endPlug);
        this.startPlug = createStartPlug(startPlug);
        this.setRoomType(RoomType.DATA_FILE);
    }

    private StartPlug createStartPlug(String plugId) {
        StartPlug startPlug = new StartPlug(plugId);
        startPlug.setExtractButton(true);

        startPlug.setListener(new PlugListener(startPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataFileType getType() {
        return type;
    }

    public void setType(DataFileType type) {
        this.type = type;
        type.getProperties().initPropertyMap(propertyMap);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public LinePlug getEndPlug() {
        return endPlug;
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public LinePlug getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    public HasDataFile getOwner() {
        return owner;
    }

    public void setOwner(HasDataFile owner) {
        this.owner = owner;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    @Override
    public Properties getProperties() {
        return type.getProperties();
    }

    @Override
    public String getSelectableId() {
        return "df" + id;
    }
}
