package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class FloorData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121909996660020L;

    private int index;

    private List<RoomData> roomList;

}
