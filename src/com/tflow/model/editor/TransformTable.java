package com.tflow.model.editor;

import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.List;

public class TransformTable extends DataTable {
    private static final long serialVersionUID = 2021121709996660040L;

    private SourceType sourceType;
    private int sourceId;
    private List<TableFx> fxList;

    public TransformTable(int id, String name, int sourceId, SourceType sourceType, String idColName, String endPlug, String startPlug) {
        super(id, name, null, null, null, idColName, false, endPlug, startPlug);
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        fxList = new ArrayList<>();
        this.setRoomType(RoomType.TRANSFORM_TABLE);
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

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_TABLE;
    }
}
