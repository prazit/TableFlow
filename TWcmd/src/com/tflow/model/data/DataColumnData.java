package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataColumnData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660031L;

    private int id;
    protected int index;
    protected String type;
    protected String name;

    protected LinePlugData startPlug;
}
