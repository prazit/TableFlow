package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinaryFileDataDev extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660024L;

    int id;
    String name;
    FileNameExtension ext;
    String content;

}
