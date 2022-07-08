package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class StepItemData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660009L;

    private int id;
    private String name;

}
