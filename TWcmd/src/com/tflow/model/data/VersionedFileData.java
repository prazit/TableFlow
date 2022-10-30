package com.tflow.model.data;

import lombok.Data;

import java.util.Date;

@Data
public class VersionedFileData {
    private static final transient long serialVersionUID = 2021121709996660018L;

    private String id;
    private String name;
    private Date uploadedDate;

}
