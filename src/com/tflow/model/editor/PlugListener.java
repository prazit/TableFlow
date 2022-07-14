package com.tflow.model.editor;

public abstract class PlugListener {

    protected LinePlug plug;

    public PlugListener(LinePlug plug) {
        this.plug = plug;
    }

    /**
     * Use to set state of the plug.<br/>
     * Invoked when a line added into the plug.<br/>
     *
     * @param line plugged line
     */
    public abstract void plugged(Line line);

    /**
     * Use to set state of the plug.
     * Invoked when a line removed from the plug.<br/>
     *
     * @param line plugged line
     */
    public abstract void unplugged(Line line);

}
