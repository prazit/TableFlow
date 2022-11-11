package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
import com.tflow.model.editor.view.PropertyView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataFile extends Room implements Selectable, HasEndPlug, HasEvent {
    private transient Logger log = LoggerFactory.getLogger(DataFile.class);

    protected DataSourceType dataSourceType;
    protected int dataSourceId;

    protected DataFileType type;

    protected int id;
    protected String name;

    @Deprecated
    protected String path;

    protected int uploadedId;

    protected Map<String, Object> propertyMap;

    protected LinePlug endPlug;
    protected LinePlug startPlug;

    protected EventManager eventManager;

    protected HasDataFile owner;

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
        propertyMap = new HashMap<>();
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) throws Exception {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.type.equals(property.getVar())) {
                    /*DataFileType CHANGED need to set default DataSourceType required by BuildPackageCommand*/
                    dataSourceType = type.getDataSourceType();
                } else if (PropertyVar.editable.equals(property.getVar())) {
                    /*editable CHANGED need to reset queryId for DataFileType.IN_SQLDB*/
                    propertyMap.remove(PropertyVar.queryId.name());
                }
            }
        });
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

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.DATA_FILE;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
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

    @Deprecated
    public String getPath() {
        return path;
    }

    @Deprecated
    public void setPath(String path) {
        this.path = path;
    }

    public int getUploadedId() {
        return uploadedId;
    }

    public void setUploadedId(int uploadedId) {
        this.uploadedId = uploadedId;
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
        return IDPrefix.DATA_FILE.getPrefix() + id;
    }

    public boolean getTypeDisabled() {
        return name != null && !name.isEmpty();
    }

    public boolean getNameDisabled() {
        return startPlug.isPlugged();
    }

    public String getDataSourceIdentifier() {
        return dataSourceType + ":" + dataSourceId;
    }

    public void setDataSourceIdentifier(String dataSourceIdentifier) {
        log.debug("setDataSourceIdentifier(dataSourceIdentifier:'{}')", dataSourceIdentifier);
        if (dataSourceIdentifier == null) {
            dataSourceType = null;
            dataSourceId = 0;
            return;
        }
        String[] parts = dataSourceIdentifier.split("[:]");
        dataSourceType = DataSourceType.valueOf(parts[0]);
        dataSourceId = Integer.parseInt(parts[1]);
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", dataSourceType:" + dataSourceType +
                ", dataSourceId:" + dataSourceId +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", uploadedId:" + uploadedId +
                ", endPlug:" + endPlug +
                ", startPlug:" + startPlug +
                '}';
    }
}
