package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransformColumnData extends DataColumnData implements Serializable {
    private static final long serialVersionUID = 2021121709996660041L;

    private String dataColName;
    private int fx;

    private LinePlugData endPlug;
}
