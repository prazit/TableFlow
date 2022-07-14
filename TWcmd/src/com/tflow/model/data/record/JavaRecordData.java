package com.tflow.model.data.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class JavaRecordData implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660002L;

    private byte[] data;
    /*TODO: change type to RecordAttributesData*/
    private RecordAttributes additional;
}
