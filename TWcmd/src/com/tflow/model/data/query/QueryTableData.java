package com.tflow.model.data.query;

import com.tflow.model.data.LinePlugData;
import com.tflow.model.data.RoomData;
import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryTableData extends RoomData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660081L;

    private int id;
    private String name;
    private String alias;
    private String schema;

    private TableJoinType joinType;
    private String joinTable;
    private int joinTableId;
    private String joinCondition;

    private LinePlugData startPlug;
    private LinePlugData endPlug;

    private List<QueryColumnData> columnList;
}
