package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OutputFileData extends DataFileData {
    private static final transient long serialVersionUID = 2021121709996660021L;

    private String dataSourceType;
    private int dataSourceId;

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", path:'" + path + '\'' +
                ", uploadedId:" + uploadedId +
                ", endPlug:" + endPlug +
                ", startPlug:" + startPlug +
                ", dataSourceType:" + dataSourceType +
                ", dataSourceId:" + dataSourceId +
                '}';
    }

}
