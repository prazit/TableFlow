package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupListData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660007L;

    private int lastProjectId;
    private List<GroupItemData> groupList;
}
