package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ColumnFxTableData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660044L;

    private List<Integer> columnFxList;
}
