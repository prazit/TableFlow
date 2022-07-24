package com.tflow.model.data;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class LinePlugData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660050L;

    private String plug;

    private boolean plugged;
    private List<Integer> lineList;

    private String removeButtonTip;
    private boolean removeButton;
    private boolean extractButton;
    private boolean transferButton;
    private boolean locked;

    private boolean notEndPlug;
}
