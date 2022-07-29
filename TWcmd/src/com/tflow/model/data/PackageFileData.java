package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageFileData extends TWData {

    boolean updated;
    String path;
    String name;
    FileType type;

    Date modifiedDate;

}
