package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class StepItemData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660009L;

    private int id;
    private String name;

}
