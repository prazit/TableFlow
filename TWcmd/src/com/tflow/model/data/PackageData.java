package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageData extends TWData {
    private static final transient long serialVersionUID = 2021121709996660061L;

    private int id;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

    private List<PackageFileData> fileList;
    private int lastFileId;

}
