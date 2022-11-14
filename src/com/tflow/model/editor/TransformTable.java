package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.data.SourceType;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.room.RoomType;
import com.tflow.model.editor.view.PropertyView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransformTable extends DataTable implements HasEvent, HasSelected {

    private SourceType sourceType;
    private int sourceId;
    private List<TableFx> fxList;

    @Deprecated
    private ColumnFxTable columnFxTable;

    /*view only*/
    private List<NameValue> quickColumnList;

    private EventManager eventManager;

    /*for ProjectMapper*/
    public TransformTable() {
        init();
    }

    public TransformTable(String name, int sourceTableId, SourceType sourceType, String idColName, String endPlug, String startPlug, Step owner) {
        super(name, null, idColName, endPlug, startPlug, owner);
        this.sourceId = sourceTableId;
        this.sourceType = sourceType;
        init();
    }

    private void init() {
        setRoomType(RoomType.TRANSFORM_TABLE);
        fxList = new ArrayList<>();
        columnFxTable = new ColumnFxTable(this);
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.columnList.equals(property.getVar())) {
                    eventManager.fireEvent(EventName.COLUMN_LIST_CHANGED, property);
                }
            }
        });
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public List<TableFx> getFxList() {
        return fxList;
    }

    public void setFxList(List<TableFx> fxList) {
        this.fxList = fxList;
    }

    public ColumnFxTable getColumnFxTable() {
        return columnFxTable;
    }

    public void setColumnFxTable(ColumnFxTable columnFxTable) {
        this.columnFxTable = columnFxTable;
    }

    @Override
    public List<DataColumn> getColumnList() {
        return super.getColumnList();
    }

    public List<NameValue> getQuickColumnList() {
        return quickColumnList;
    }

    public void setQuickColumnList(List<NameValue> quickColumnList) {
        this.quickColumnList = quickColumnList;
    }

    public void refreshQuickColumnList() {
        if (columnList.size() == 0) return;

        quickColumnList = new ArrayList<>();
        int index = 0;
        TransformColumn column;
        for (DataColumn dataColumn : columnList) {
            column = (TransformColumn) dataColumn;
            quickColumnList.add(new NameValue(column.getName(), column.getValue(), index++));
        }
        quickColumnList.get(columnList.size() - 1).setLast(true);
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.TRANSFORM_TABLE;
    }

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_TABLE;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.TRANSFORM_TABLE.getPrefix() + id;
    }

    @Override
    public void selected() {
        refreshQuickColumnList();
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
                ", sourceType:" + sourceType +
                ", sourceId:'" + sourceId + '\'' +
                ", fxList:" + Arrays.toString(fxList.toArray()) +
                ", columnFxTable:" + columnFxTable +
                '}';
    }
}
