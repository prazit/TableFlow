package com.tflow.model.data.query;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryFilterData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660083L;

    private int id;
    private int index;

    private QueryFilterConnector connector;
    private String leftValue;
    private QueryFilterOperation operation;
    private String rightValue;

}
