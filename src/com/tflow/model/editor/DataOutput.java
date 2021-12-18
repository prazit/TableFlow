package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSource;

public class DataOutput {
    private static final long serialVersionUID = 2021121709996660032L;

    private DataFile dataFile;
    private DataSource dataSource;

    private String startPlug;

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
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
}
