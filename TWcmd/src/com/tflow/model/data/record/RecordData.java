package com.tflow.model.data.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RecordData {

    private Object data;
    private RecordAttributesData additional;

}
