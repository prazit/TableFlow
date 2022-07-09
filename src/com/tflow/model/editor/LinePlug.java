package com.tflow.model.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinePlug implements Serializable {
    private static final long serialVersionUID = 2021121709996660050L;

    private String plug;

    private boolean plugged;
    private List<Line> lineList;

    private String removeButtonTip;
    private boolean removeButton;
    private boolean extractButton;
    private boolean transferButton;
    private boolean locked;

    private boolean notEndPlug;

    private PlugListener listener;

    /*for ProjectMapper*/
    public LinePlug() {
        /*nothing*/
    }

    public LinePlug(String plug) {
        this.plug = plug;
        lineList = new ArrayList<>();
        removeButtonTip = "";
        defaultPlugListener();
    }

    /**
     * This is default listener for End Plug only.
     */
    private void defaultPlugListener() {
        listener = new PlugListener(this) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                plug.setRemoveButton(plugged);
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

    public String getRemoveButtonTip() {
        return removeButtonTip;
    }

    public void setRemoveButtonTip(String removeButtonTip) {
        this.removeButtonTip = removeButtonTip;
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

    public boolean isTransferButton() {
        return transferButton;
    }

    public void setTransferButton(boolean transferButton) {
        styleClass = null;
        this.transferButton = transferButton;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        styleClass = null;
        this.locked = locked;
    }

    public boolean isNotEndPlug() {
        return notEndPlug;
    }

    public void setNotEndPlug(boolean notEndPlug) {
        styleClass = null;
        this.notEndPlug = notEndPlug;
    }

    private String styleClass;

    public String getStyleClass() {
        if (styleClass == null)
            styleClass = (notEndPlug ? "start-plug" : "end-plug") +
                    (plugged ? " connected" : " no-connection") +
                    (removeButton ? " remove-line" : "") +
                    (extractButton ? " extract-data" : "") +
                    (transferButton ? " transfer-data" : "") +
                    (locked ? " locked" : "") +
                    (locked || removeButton || extractButton || transferButton ? "" : " draggable");
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
        /*Don't change: used in UI, UI need elementId*/
        return plug;
    }
}
