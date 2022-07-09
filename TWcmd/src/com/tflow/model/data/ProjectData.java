package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProjectData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660001L;

    private String id;
    private String name;
    private int activeStepIndex;

    private int lastElementId;
    private int lastUniqueId;
}
