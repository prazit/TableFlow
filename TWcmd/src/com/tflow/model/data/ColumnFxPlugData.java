package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ColumnFxPlugData extends LinePlugData implements Serializable {
    private static final long serialVersionUID = 2021121709996660045L;

    private int id;
    private String type;
    private String name;
}
