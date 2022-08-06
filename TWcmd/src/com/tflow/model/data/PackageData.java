package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageData extends TWData {

    private String projectId;
    private int packageId;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

}
