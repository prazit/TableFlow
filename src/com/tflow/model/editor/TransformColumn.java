package com.tflow.model.editor;

public class TransformColumn extends DataColumn {
    private static final long serialVersionUID = 2021121709996660041L;

    private String dataColName;
    private ColumnFx fx;

    private String endPlug;

    public TransformColumn(DataColumn sourceColumn, String endPlug, String startPlug, DataTable owner) {
        super(sourceColumn.getIndex(), sourceColumn.getType(), sourceColumn.getName(), startPlug, owner);
        dataColName = "" + name;
        this.endPlug = endPlug;
    }

    public String getDataColName() {
        return dataColName;
    }

    public void setDataColName(String dataColName) {
        this.dataColName = dataColName;
    }

    public ColumnFx getFx() {
        return fx;
    }

    public void setFx(ColumnFx fx) {
        this.fx = fx;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_COLUMN;
    }
}
