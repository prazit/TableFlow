package com.tflow.model.editor;

import java.io.Serializable;

public class Line implements Serializable {
    private static final long serialVersionUID = 2021122109996660010L;

    private String startPlug;
    private String endPlug;
    private LineType type;

    public Line(String startPlug, String endPlug, LineType lineType) {
        this.startPlug = startPlug;
        this.endPlug = endPlug;
        this.type = lineType;
    }

    public String getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(String startPlug) {
        this.startPlug = startPlug;
    }

    public String getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(String endPlug) {
        this.endPlug = endPlug;
    }

    public LineType getType() {
        return type;
    }

    public void setType(LineType type) {
        this.type = type;
    }
}
