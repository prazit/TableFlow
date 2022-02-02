package com.tflow.model.editor;

import java.util.ArrayList;
import java.util.List;

public class LinePlug {

    private String plug;

    private boolean plugged;
    private List<Line> lineList;

    private boolean removeButton;
    private boolean extractButton;

    private boolean startPlug;

    private PlugListener listener;

    public LinePlug(String plug) {
        this.plug = plug;
        lineList = new ArrayList<>();
        defaultPlugListener();
    }

    private void defaultPlugListener() {
        /*TODO: need to remove this function when all plugs already handled*/
        listener = new PlugListener(this) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
            }

            @Override
            public void unplugged(Line line) {
                plug.setPlugged(plug.getLineList().size() > 0);
            }
        };
    }

    public String getPlug() {
        return plug;
    }

    public void setPlug(String plug) {
        styleClass = null;
        this.plug = plug;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        styleClass = null;
        this.plugged = plugged;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public Line getLine() {
        if (lineList.size() == 0) return null;
        return lineList.get(0);
    }

    public boolean isRemoveButton() {
        return removeButton;
    }

    public void setRemoveButton(boolean removeButton) {
        styleClass = null;
        this.removeButton = removeButton;
    }

    public boolean isExtractButton() {
        return extractButton;
    }

    public void setExtractButton(boolean extractButton) {
        styleClass = null;
        this.extractButton = extractButton;
    }

    public boolean isStartPlug() {
        return startPlug;
    }

    public void setStartPlug(boolean startPlug) {
        styleClass = null;
        this.startPlug = startPlug;
    }

    private String styleClass;

    public String getStyleClass() {
        if (styleClass == null)
            styleClass = (startPlug ? "start-plug" : "end-plug") +
                    (plugged ? " connected" : " no-connection") +
                    (removeButton ? " remove-line" : "") +
                    (extractButton ? " extract-data" : "") +
                    (removeButton || extractButton ? "" : " draggable");
        return styleClass;
    }

    public PlugListener getListener() {
        return listener;
    }

    public void setListener(PlugListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return plug;
    }
}
