package com.tflow.model.data.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class RecordData {

    private Object data;
    private RecordAttributesData additional;

}
