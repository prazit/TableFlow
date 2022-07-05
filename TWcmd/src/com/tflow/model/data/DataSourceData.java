package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataSourceData implements Serializable {
    private static final long serialVersionUID = 2021121709996660010L;

    protected int id;
    protected String type;
    protected String name;
    protected String image;
    protected LinePlugData plug;
}
