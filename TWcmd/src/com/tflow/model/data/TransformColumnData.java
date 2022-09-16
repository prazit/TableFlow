package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransformColumnData extends DataColumnData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660041L;

    private int sourceColumnId;
    private String dynamicExpression;
    private boolean useDynamic;

    private boolean useFunction;
    private String function;
    private Map<String, Object> propertyMap;
    private String propertyOrder;

    @Deprecated
    private int fx;

    @Deprecated
    private LinePlugData endPlug;
}
