package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataTableData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660030L;

    private int id;
    private String name;
    private int index;
    private int level;
    private int dataFile;
    private String query;
    private String idColName;

    private boolean noTransform;

    private LinePlugData endPlug;
    private LinePlugData startPlug;
    private int connectionCount;
}
