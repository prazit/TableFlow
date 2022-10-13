package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinaryFileDataDev extends TWData {

    int id;
    String name;
    FileNameExtension ext;
    String content;

}
