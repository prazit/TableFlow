package com.tflow.model.data;

import lombok.Data;

@Data
public class DataSourceData {
    private static final long serialVersionUID = 2021121709996660010L;

    protected int id;
    protected String type;
    protected String name;
    protected String image;
    protected LinePlugData plug;
}
