package com.tflow.model.data.query;

import com.tflow.model.data.LinePlugData;
import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryTableData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660081L;

    private int id;
    private String name;

    private LinePlugData startPlug;
    private LinePlugData endPlug;

}
