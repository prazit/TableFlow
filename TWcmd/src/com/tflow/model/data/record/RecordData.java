package com.tflow.model.data.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RecordData {
    private static final transient long serialVersionUID = 2021121709996660071L;

    private Object data;
    private RecordAttributesData additional;

}
