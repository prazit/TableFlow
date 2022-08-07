package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageFileData extends TWData {

    int id;
    String name;
    FileNameExtension ext;

    FileType type;
    int fileId;

    Date buildDate;
    String buildPath;

    boolean updated;

}
