package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataColumnData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660031L;

    private int id;
    protected int index;
    protected String type;
    protected String name;

    protected LinePlugData startPlug;
}
