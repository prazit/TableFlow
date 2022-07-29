package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PackageItemData extends TWData {

    int packageId;
    String name;

}
