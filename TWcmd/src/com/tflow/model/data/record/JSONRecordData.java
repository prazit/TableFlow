package com.tflow.model.data.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class JSONRecordData implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660002L;

    private String dataClass;
    private Object data;
    private RecordAttributesData additional;
}
