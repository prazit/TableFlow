package com.tflow.model.data;

import lombok.Data;

import java.util.List;

@Data
public class ProjectData {
    private static final long serialVersionUID = 2021121709996660001L;

    private String id;
    private String name;
    private int activeStepIndex;

    private List<Integer> stepList;
    private List<Integer> databaseList;

    private List<Integer> sftpList;
    private List<Integer> localList;
    private List<String> variableList;

    private int lastElementId;
    private int lastUniqueId;
}
