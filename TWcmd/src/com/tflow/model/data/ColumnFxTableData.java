package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ColumnFxTableData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660044L;

    private List<Integer> columnFxList;
}
