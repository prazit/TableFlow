package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class LineData implements Serializable {
    private static final long serialVersionUID = 2021121709996660054L;

    private int id;
    private String startSelectableId;
    private String endSelectableId;

    private int clientIndex;
    private LinePlugData startPlug;
    private LinePlugData endPlug;
    private String type;
    private String text;

    private boolean user;

}
