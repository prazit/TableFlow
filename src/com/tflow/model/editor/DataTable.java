package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataTable extends Room implements Selectable, HasDataFile, HasEndPlug {

    protected int id;
    protected String name;
    protected int index;
    protected int level;
    protected DataFile dataFile;
    protected String query;
    protected String idColName;
    protected List<DataColumn> columnList;
    protected List<OutputFile> outputList;

    /*noTransform can use Auto Generated Value*/
    protected boolean noTransform;

    protected LinePlug endPlug;
    protected LinePlug startPlug;
    protected int connectionCount;

    protected Step owner;

    /*for ProjectMapper*/
    public DataTable() {
        /*nothing*/
    }

    public DataTable(String name, DataFile dataFile, String idColName, String endPlug, String startPlug, Step owner) {
        this.owner = owner;
        this.name = name;
        this.index = -1;
        this.dataFile = dataFile;
        if (dataFile != null) {
            dataFile.setOwner(this);
        }
        this.query = "";
        this.idColName = idColName;
        this.noTransform = false;
        createEndPlug(endPlug);
        createStartPlug(startPlug);
        this.startPlug.setTransferButton(true);
        connectionCount = 0;
        this.columnList = new ArrayList<>();
        this.outputList = new ArrayList<>();
        this.setRoomType(RoomType.DATA_TABLE);
    }

    private void createEndPlug(String plugId) {
        endPlug = new EndPlug(plugId);
        createEndPlugListener();
    }

    private void createEndPlugListener() {
        endPlug.setListener(new PlugListener(endPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
                plug.setRemoveButtonTip("Remove This Table");
            }

            @Override
            public void unplugged(Line line) {
                plug.setPlugged(false);
                plug.setRemoveButton(false);
            }
        });
    }

    private void createStartPlug(String plugId) {
        startPlug = new StartPlug(plugId);
        startPlug.setTransferButton(true);
        createStartPlugListener();
    }

    private void createStartPlugListener() {
        startPlug.setListener(new PlugListener(startPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                connectionCreated();
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                connectionRemoved();
            }
        });
    }

    /*call after ProjectMapper*/
    public void createPlugListeners() {
        createStartPlugListener();
        createEndPlugListener();
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.DATA_TABLE;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    @Override
    public boolean isDataTable() {
        return true;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getIdColName() {
        return idColName;
    }

    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }

    public List<DataColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<DataColumn> columnList) {
        this.columnList = columnList;
    }

    public List<OutputFile> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<OutputFile> outputList) {
        this.outputList = outputList;
    }

    public boolean isNoTransform() {
        return noTransform;
    }

    public void setNoTransform(boolean noTransform) {
        this.noTransform = noTransform;
    }

    public Step getOwner() {
        return owner;
    }

    public void setOwner(Step owner) {
        this.owner = owner;
    }

    /**
     * every child need to call this function after plugged.
     */
    public void connectionCreated() {
        connectionCount += 1;
        connectionUpdated();
    }

    /**
     * every child need to call this function after unplugged.
     */
    public void connectionRemoved() {
        connectionCount -= 1;
        connectionUpdated();
    }

    private void connectionUpdated() {
        boolean locked = hasConnection();

        /*endPlug need to lock when some connections are created*/
        /*endPlug need to unlock/show-remove-button when all connections are removed*/
        endPlug.setLocked(locked);
        endPlug.setRemoveButton(!locked);

        Logger log = LoggerFactory.getLogger(getClass());
        log.warn("connectionUpdated(table:{}, connectionCount:{}).", getSelectableId(), connectionCount);
    }

    private boolean hasConnection() {
        return connectionCount > 0;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
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

    @Override
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_TABLE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.DATA_TABLE.getPrefix() + id;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", index:" + index +
                ", level:" + level +
                ", idColName:'" + idColName + '\'' +
                ", noTransform:" + noTransform +
                ", endPlug:" + endPlug +
                ", startPlug:" + startPlug +
                ", connectionCount:" + connectionCount +
                ", columnList:" + Arrays.toString(columnList.toArray()) +
                ", outputList:" + Arrays.toString(outputList.toArray()) +
                '}';
    }

}
