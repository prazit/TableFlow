package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FloorData extends TWData implements Serializable {
    private static final long serialVersionUID = 2021121909996660020L;

    private int id;
    private int index;
    private int tower;

    private List<RoomData> roomList;

}
