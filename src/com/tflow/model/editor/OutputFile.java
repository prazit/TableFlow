package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;

public class OutputFile extends DataFile {
    private DataSourceType dataSourceType;
    private int dataSourceId;

    /*for ProjectMapper*/
    public OutputFile() {
        /*nothing*/
    }

    public OutputFile(DataFileType type, String path, String endPlug, String startPlug) {
        super(type, path, endPlug, startPlug);
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

    @Override
    public String getSelectableId() {
        return "of" + id;
    }
}
