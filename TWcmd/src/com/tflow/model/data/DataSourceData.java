package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataSourceData extends RoomData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660010L;

    protected int id;
    protected String type;
    protected String name;
    protected String image;

}
