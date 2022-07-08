package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TODO: after getData
 * 1. call createStartPlug after regenSelectableMap
 * 2. call createEndPlug after regenSelectableMap
 **/
@Data
public class ColumnFxData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660042L;

    private int id;
    private String name;
    private String function;
    private Map<String, Object> propertyMap;

    private List<ColumnFxPlugData> endPlugList;
    private LinePlugData startPlug;
}
