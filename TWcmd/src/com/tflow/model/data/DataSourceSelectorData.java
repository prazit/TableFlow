package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataSourceSelectorData extends DataSourceData {
    private static final transient long serialVersionUID = 2021121709996660015L;

    private int dataSourceId;
    private LinePlugData startPlug;

}
