package com.tflow.model.editor;

import java.io.Serializable;

public class Line implements Serializable {
    private static final long serialVersionUID = 2021122109996660010L;

    private String startSelectableId;
    private String endSelectableId;

    private int clientIndex;
    private LinePlug startPlug;
    private LinePlug endPlug;
    private LineType type;

    public Line(String startSelectableId, String endSelectableId) {
        this.startSelectableId = startSelectableId;
        this.endSelectableId = endSelectableId;
    }

    public int getClientIndex() {
        return clientIndex;
    }

    public void setClientIndex(int clientIndex) {
        this.clientIndex = clientIndex;
    }

    public String getStartSelectableId() {
        return startSelectableId;
    }

    public void setStartSelectableId(String startSelectableId) {
        this.startSelectableId = startSelectableId;
    }

    public String getEndSelectableId() {
        return endSelectableId;
    }

    public void setEndSelectableId(String endSelectableId) {
        this.endSelectableId = endSelectableId;
    }

    public LinePlug getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    public LinePlug getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    public LineType getType() {
        return type;
    }

    public void setType(LineType type) {
        this.type = type;
    }

    public String getJsAdd() {
        return String.format("lines[%d] = new LeaderLine(document.getElementById('%s'), document.getElementById('%s'), %s);",
                clientIndex,
                startPlug,
                endPlug,
                type.getJsVar()
        );
    }

    public String getJsRemove() {
        return String.format("lines[%d].remove(); lines[%d] = undefined;",
                clientIndex,
                clientIndex
        );
    }
}
