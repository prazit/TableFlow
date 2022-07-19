package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OutputFileData extends DataFileData {
    private static final transient long serialVersionUID = 2021121709996660021L;

    private String dataSourceType;
    private int dataSourceId;

}
