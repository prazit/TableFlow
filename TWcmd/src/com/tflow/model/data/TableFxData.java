package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class TableFxData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660043L;

    private int id;
    private String name;

    private boolean useFunction;
    private String function;
    private Map<String, Object> propertyMap;
    private String propertyOrder;
}
