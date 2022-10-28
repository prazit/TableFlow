package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ItemData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660009L;

    private int id;
    private String name;

    /* for extended class */
    public ItemData() {
        /*nohting*/
    }

    public ItemData(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
