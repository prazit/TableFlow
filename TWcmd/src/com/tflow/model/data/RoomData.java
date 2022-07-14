package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoomData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121909996660030L;

    private String elementId;
    private int roomIndex;
    private String roomType;

    /*Notice: Optional: Remove or use as Checker*/
    private String selectableId;

}
