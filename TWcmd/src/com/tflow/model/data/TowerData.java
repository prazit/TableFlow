package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TowerData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121909996660010L;

    private int id;
    private List<Integer> floorList;
}
