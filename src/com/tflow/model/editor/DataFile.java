package com.tflow.model.editor;

import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataFile extends Room implements Selectable, HasEndPlug {
    private transient Logger log = LoggerFactory.getLogger(DataFile.class);

    protected int id;
    private DataFileType type;
    private String name;
    private String path;

    private Map<String, Object> propertyMap;

    private LinePlug endPlug;
    private LinePlug startPlug;

    private HasDataFile owner;

    /*for projectMapper*/
    public DataFile() {
        init();
    }

    /* for projectMapper */
    public DataFile(Integer id) {
        this.id = id;
        init();
    }

    public DataFile(DataFileType type, String path, String endPlug, String startPlug) {
        this.type = type;
        this.name = type.getDefaultName();
        this.path = path;
        this.endPlug = new EndPlug(endPlug);
        createStartPlug(startPlug);
        init();
        type.getProperties().initPropertyMap(propertyMap);
    }

    private void init() {
        this.propertyMap = new HashMap<>();
        this.setRoomType(RoomType.DATA_FILE);
    }

    private void createStartPlug(String plugId) {
        startPlug = new StartPlug(plugId);
        startPlug.setExtractButton(true);
        createStartPlugListener();
    }

    private void createStartPlugListener() {
        endPlug.setRemoveButtonTip("Remove DataFile");
        startPlug.setListener(new PlugListener(startPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                endPlug.setRemoveButton(false);
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                endPlug.setRemoveButton(true);
            }
        });
    }

    public void createPlugListeners() {
        createStartPlugListener();
        endPlug.createDefaultPlugListener();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", path:'" + path + '\'' +
                ", endPlug:" + endPlug +
                ", startPlug:" + startPlug +
                '}';
    }
}
