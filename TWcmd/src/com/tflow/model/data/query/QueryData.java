package com.tflow.model.data.query;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660080L;

    private int id;
    private String name;

    private int tower;

}
