package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransformColumnData extends DataColumnData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660041L;

    private String dataColName;
    private int fx;

    private LinePlugData endPlug;
}
