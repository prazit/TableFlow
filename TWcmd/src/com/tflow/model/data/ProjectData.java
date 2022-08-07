package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660001L;

    private String id;
    private String name;
    private ProjectType type;
    private int activeStepIndex;

    private int lastElementId;
    private int lastUniqueId;
}
