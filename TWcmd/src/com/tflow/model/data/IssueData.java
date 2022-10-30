package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IssueData {
    private static final transient long serialVersionUID = 2021121709996660017L;

    private int id;
    private String type;

    private int stepId;
    private String objectType;
    private String objectId;
    private String propertyVar;

}
