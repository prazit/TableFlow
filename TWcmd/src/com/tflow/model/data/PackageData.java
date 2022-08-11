package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageData extends TWData {

    private int id;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

    private List<PackageFileData> fileList;
    private int lastFileId;

}
