package com.tflow.model.editor;

public class LinePlug {

    private String plug;
    private boolean plugged;
    private boolean removeButton;
    private boolean extractButton;
    private boolean startPlug;

    public LinePlug() {
        plug = "";
    }

    public LinePlug(String plug) {
        this.plug = plug;
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

    @Override
    public String toString() {
        return plug;
    }
}
