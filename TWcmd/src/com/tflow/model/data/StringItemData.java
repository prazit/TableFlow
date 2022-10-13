package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class StringItemData  extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660008L;

    private String id;
    private String name;

    public StringItemData() {
        /*nothing*/
    }

    public StringItemData(String id, String name) {
        this.id = id;
        this.name = name;
    }
}