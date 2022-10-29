package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IssueData extends ItemData {

    private String description;

    private int stepId;
    private String objectId;
    private String propertyVar;

}
