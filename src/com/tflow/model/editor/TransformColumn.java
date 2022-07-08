package com.tflow.model.editor;

public class TransformColumn extends DataColumn implements HasEndPlug {
    private static final long serialVersionUID = 2021121709996660041L;

    private String dataColName;
    private ColumnFx fx;

    private LinePlug endPlug;

    /*for projectMapper*/
    public TransformColumn() {/*nothing*/}

    public TransformColumn(DataColumn sourceColumn, String endPlug, String startPlug, DataTable owner) {
        super(sourceColumn.getIndex(), sourceColumn.getType(), sourceColumn.getName(), startPlug, owner);
        dataColName = "" + name;
        this.endPlug = createEndPlug(endPlug);
    }

    public TransformColumn(int index, DataType type, String name, String endPlug, String startPlug, DataTable owner) {
        super(index, type, name, startPlug, owner);
        dataColName = "" + name;
        this.endPlug = createEndPlug(endPlug);
    }

    private LinePlug createEndPlug(String endPlug) {
        LinePlug plug = new EndPlug(endPlug);
        plug.setListener(new PlugListener(plug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
                owner.connectionCreated();
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                plug.setRemoveButton(plugged);
                owner.connectionRemoved();
            }
        });
        return plug;
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

    @Override
    public LinePlug getEndPlug() {
        return endPlug;
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_COLUMN;
    }
}
