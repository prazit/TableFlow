package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageFileData extends TWData {
    private static final transient long serialVersionUID = 2021121709996660062L;

    int id;
    String name;
    FileNameExtension ext;

    FileType type;
    int fileId;
    String buildPath;

    boolean updated;

}
