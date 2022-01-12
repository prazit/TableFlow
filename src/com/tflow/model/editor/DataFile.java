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
    private String image;
    private String name;
    private String path;

    private Map<String, Object> propertyMap;

    private String endPlug;
    private String startPlug;

    private HasDataFile owner;

    public DataFile(DataSource dataSource, DataFileType type, String name, String path, String endPlug, String startPlug) {
        this.dataSource = dataSource;
        this.type = type;
        this.name = name;
        this.path = path;
        this.image = type.getImage();
        this.propertyMap = new HashMap<>();
        initPropertyMap();
        this.endPlug = endPlug;
        this.startPlug = startPlug;
        this.setRoomType(RoomType.DATA_FILE);
    }

    private void initPropertyMap() {
        for (String property : type.getProperties().getPrototypeList()) {
            String[] params = property.split("[:]");
            if (params[0].equals(".")) {
                if (!propertyMap.containsKey(params[1]))
                    propertyMap.put(params[1], PropertyType.valueOf(params[4].toUpperCase()).getInitial());
            }
        }
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
        initPropertyMap();
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

    @Override
    public String getEndPlug() {
        return endPlug;
    }

    @Override
    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public String getStartPlug() {
        return startPlug;
    }

    @Override
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
        return "df" + id;
    }
}
