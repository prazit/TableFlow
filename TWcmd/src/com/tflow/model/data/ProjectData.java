package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProjectData implements Serializable {
    private static final long serialVersionUID = 2021121709996660001L;

    private String id;
    private String name;
    private int activeStepIndex;

    private List<StepItemData> stepList;
    private List<Integer> databaseList;

    private List<Integer> sftpList;
    private List<Integer> localList;
    private List<String> variableList;

    private int lastElementId;
    private int lastUniqueId;
}
