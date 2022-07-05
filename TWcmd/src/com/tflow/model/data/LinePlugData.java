package com.tflow.model.data;


import lombok.Data;

import java.util.List;

/*TODO: after getData need to call Owner.createPlugListener*/
@Data
public class LinePlugData {
    private static final long serialVersionUID = 2021121709996660050L;

    private String plug;

    private boolean plugged;
    private List<Integer> lineList;

    private String removeButtonTip;
    private boolean removeButton;
    private boolean extractButton;
    private boolean transferButton;
    private boolean locked;

    private boolean startPlug;
}
