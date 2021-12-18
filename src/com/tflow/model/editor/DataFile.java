package com.tflow.model.editor;

import java.io.Serializable;
import java.util.Map;

public class DataFile  implements Serializable {
    private static final long serialVersionUID = 2021121709996660020L;

    private DataFileType type;
    private String name;
    private Map<String,String> paramMap;

    private String endPlug;
    private String startPlug;

    public DataFileType getType() {
        return type;
    }

    public void setType(DataFileType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }
}
