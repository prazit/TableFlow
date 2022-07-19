package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataFileData extends RoomData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660020L;

    private int id;
    private String type;
    private String name;
    private String path;

    private Map<String, Object> propertyMap;

    private LinePlugData endPlug;
    private LinePlugData startPlug;

}
