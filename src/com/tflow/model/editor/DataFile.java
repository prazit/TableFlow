package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataFile extends Room implements Serializable, Selectable {
    private static final long serialVersionUID = 2021121709996660020L;

    private Logger log = LoggerFactory.getLogger(DataFile.class);

    private HasDataFile owner;
    private DataSource dataSource;
    private DataFileType type;
    private String image;
    private String name;
    private String path;

    private Map<String, Object> propertyMap;

    private String endPlug;
    private String startPlug;

    public DataFile(DataSource dataSource, DataFileType type, String name, String path, String endPlug, String startPlug) {
        this.dataSource = dataSource;
        this.type = type;
        this.name = name;
        this.path = path;
        this.image = type.getImage();
        this.propertyMap = initPropertyMap(type);
        this.endPlug = endPlug;
        this.startPlug = startPlug;
        this.setRoomType(RoomType.DATA_FILE);
    }

    private Map<String, Object> initPropertyMap(DataFileType type) {
        Map<String, Object> resultMap = new HashMap<>();
        for (String property : type.getProperties().getPrototypeList()) {
            String[] params = property.split("[:]");
            if (params[0].equals(".")) {
                resultMap.put(params[1], PropertyType.valueOf(params[4].toUpperCase()).getInitial());
            } else {
                resultMap.put(params[0], PropertyType.valueOf(params[2].toUpperCase()).getInitial());
            }
        }
        log.warn("initPropertyMap={}", resultMap);
        return resultMap;
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
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    public HasDataFile getOwner() {
        return owner;
    }

    public void setOwner(HasDataFile owner) {
        this.owner = owner;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    @Override
    public Properties getProperties() {
        return type.getProperties();
    }

    @Override
    public String getSelectableId() {
        if (name == null) return "";
        return name.replaceAll("[ .]", "_");
    }
}
