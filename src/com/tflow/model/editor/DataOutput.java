package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Room;

public class DataOutput implements Selectable, HasDataFile {
    private static final long serialVersionUID = 2021121709996660032L;

    /**
     * Owner room is DataTable or TransformTable.
     */
    private Room owner;

    private DataFile dataFile;
    private DataSource dataSource;

    private String startPlug;

    public DataOutput(Room owner, DataFile dataFile, DataSource dataSource, String startPlug) {
        this.owner = owner;
        this.dataFile = dataFile;
        if(dataFile != null) {
            dataFile.setOwner(this);
        }
        this.dataSource = dataSource;
        this.startPlug = startPlug;
    }

    public Room getOwner() {
        return owner;
    }

    public void setOwner(Room owner) {
        this.owner = owner;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    @Override
    public boolean isDataTable() {
        return false;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_OUTPUT;
    }

    @Override
    public String getSelectableId() {
        if (dataFile == null || dataSource == null) return "";
        return ((Selectable) owner).getSelectableId() + dataSource.getId() + dataFile.getSelectableId();
    }
}
