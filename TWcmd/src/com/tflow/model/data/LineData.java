package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class LineData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660054L;

    private int id;
    private String startSelectableId;
    private String endSelectableId;

    private int clientIndex;
    private String type;
    private String text;

    private boolean user;

}
