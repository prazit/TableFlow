package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupItemData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660005L;
    
    private int id;
    private String name;
}
