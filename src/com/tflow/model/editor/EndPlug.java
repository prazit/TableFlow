package com.tflow.model.editor;

public class EndPlug extends LinePlug {
    private static final long serialVersionUID = 2021121709996660051L;

    public EndPlug(String elementId) {
        super(elementId);
        setStartPlug(false);
    }
}
