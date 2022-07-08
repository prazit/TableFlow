package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121909996660030L;

    private String elementId;
    private int roomIndex;
    private String roomType;

    /*Notice: Optional: Remove or use as Checker*/
    private String selectableId;

}
