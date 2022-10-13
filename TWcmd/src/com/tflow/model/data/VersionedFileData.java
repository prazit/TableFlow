package com.tflow.model.data;

import lombok.Data;

import java.util.Date;

@Data
public class VersionedFileData {

    private String id;
    private String name;
    private Date uploadedDate;

}
