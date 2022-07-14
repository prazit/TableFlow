package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ColumnFxPlugData extends LinePlugData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660045L;

    private int id;
    private String type;
    private String name;
}
