package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

/* TODO: need to complete all fields in StepData Model */
@Data
public class StepData implements Serializable {
    private static final long serialVersionUID = 2021121709996660002L;

    private int id;
    private String name;
    private int index;

    /*private List<Action> history;
    private List<DataTable> dataList;
    private List<TransformTable> transformList;
    private List<DataFile> outputList;*/

    /*private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;*/

    /*private List<Line> lineList;
    private int lastLineClientIndex;

    private LinePlug startPlug;

    private Project owner;

    private Selectable activeObject;*/
    private Double zoom;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private int stepListActiveTab;

    /*private Map<String, Selectable> selectableMap;*/
}
