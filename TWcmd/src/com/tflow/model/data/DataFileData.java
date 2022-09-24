package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataFileData extends RoomData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660020L;

    protected String dataSourceType;
    protected int dataSourceId;

    protected int id;
    protected String type;
    protected String name;
    protected String path;

    protected int uploadedId;

    protected Map<String, Object> propertyMap;

    protected LinePlugData endPlug;
    protected LinePlugData startPlug;

}
