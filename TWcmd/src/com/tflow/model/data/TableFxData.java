package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class TableFxData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660043L;

    private int id;
    private String name;
    private String function;
    private Map<String, Object> paramMap;
}
