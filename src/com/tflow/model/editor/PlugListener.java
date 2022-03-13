package com.tflow.model.editor;

import java.io.Serializable;

public abstract class PlugListener implements Serializable {
    private static final long serialVersionUID = 2021121709996660053L;

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
