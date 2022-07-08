package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class StepData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121709996660002L;

    private int id;
    private String name;
    private int index;

    private int dataTower;
    private int transformTower;
    private int outputTower;

    private String activeObject;

    private Double zoom;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private int stepListActiveTab;
}
