package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class StepData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660002L;

    private int id;
    private String name;
    private int index;

    private int dataTower;
    private int transformTower;
    private int outputTower;
    private int lastLineClientIndex;

    private String activeObject;

    private Double zoom;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private boolean showColumnNumbers;
    private int stepListActiveTab;
}
