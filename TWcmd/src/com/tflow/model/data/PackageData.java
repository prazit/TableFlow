package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageData extends TWData {
    private static final transient long serialVersionUID = 2021121709996660061L;

    private boolean lock;
    private int id;
    private PackageType type;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;
    private boolean finished;

    private List<PackageFileData> fileList;
    private int lastFileId;

}
