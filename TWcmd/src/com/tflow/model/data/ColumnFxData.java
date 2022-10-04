package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Deprecated
@Data
@EqualsAndHashCode(callSuper = false)
public class ColumnFxData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660042L;

    private int id;
    private String name;
    private String function;
    private Map<String, Object> propertyMap;

    private List<ColumnFxPlugData> endPlugList;
    private LinePlugData startPlug;
}
