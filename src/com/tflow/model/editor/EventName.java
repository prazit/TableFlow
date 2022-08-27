package com.tflow.model.editor;

public enum EventName {

    /*Event as command trigger*/
    REMOVE,
    ADD_LINE,
    REMOVE_LINE,

    /*Event as status notifier*/
    PROPERTY_CHANGED,
    NAME_CHANGED,
    LINE_REMOVED,
    LINE_ADDED

}
