package com.tflow.model.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TowerData implements Serializable {
    private static final long serialVersionUID = 2021121909996660010L;

    private int id;
    private List<Integer> floorList;
}
