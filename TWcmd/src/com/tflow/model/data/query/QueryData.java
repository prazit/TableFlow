package com.tflow.model.data.query;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660080L;

    private int id;
    private String name;

    private int tower;

    private List<String> schemaList;

    private List<QueryColumnData> columnList;
}
