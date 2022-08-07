package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinaryFileData extends TWData {

    int id;
    String name;
    FileNameExtension ext;
    byte[] content;

}
