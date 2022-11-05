package com.tflow.model.data.query;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueryColumnData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660082L;

    private int id;
    private int index;
    private String name;
    private String value;

    private boolean selected;

}
