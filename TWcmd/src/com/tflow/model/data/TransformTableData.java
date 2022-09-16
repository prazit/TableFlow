package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransformTableData extends DataTableData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660040L;

    private String sourceType;
    private int sourceId;

}
