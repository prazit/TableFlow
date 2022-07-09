package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransformTableData extends DataTableData implements Serializable {
    private static final long serialVersionUID = 2021121709996660040L;

    private String sourceType;
    private String sourceSelectableId;

}
