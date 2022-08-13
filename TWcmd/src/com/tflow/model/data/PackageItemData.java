package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageItemData extends TWData {
    private static final transient long serialVersionUID = 2021121709996660063L;

    int packageId;
    String name;

}
