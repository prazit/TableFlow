package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectItemData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660003L;

    private String id;
    private String name;
}
