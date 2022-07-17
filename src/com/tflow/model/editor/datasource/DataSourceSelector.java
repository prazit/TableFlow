package com.tflow.model.editor.datasource;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.RoomType;

import java.util.HashMap;
import java.util.Map;

public class DataSourceSelector extends DataSource implements Selectable {

    private int dataSourceId;
    private LinePlug startPlug;

    /*for ProjectMapper*/
    public DataSourceSelector() {
        init();
    }

    public DataSourceSelector(String name, DataSourceType dataSourceType, String startPlug) {
        this.name = name;
        this.type = dataSourceType;
        this.startPlug = createStartPlug(startPlug);
        init();
    }

    private void init() {
        this.dataSourceId = -1;
        this.setRoomType(RoomType.DATA_SOURCE);
        this.image = "local.png";
    }

    private StartPlug createStartPlug(String plugId) {
        StartPlug startPlug = new StartPlug(plugId);
        startPlug.setExtractButton(true);

        startPlug.setListener(new PlugListener(startPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
            }
        });

        return startPlug;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @Override
    public Properties getProperties() {
        return Properties.STEP_DATA_SOURCE;
    }

    @Override
    public String getSelectableId() {
        return "ds" + id;
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
}
