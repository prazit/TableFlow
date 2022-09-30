package com.tflow.model.editor;

public enum EventName {

    /*Event as command trigger*/
    REMOVE,
    ADD_LINE,
    REMOVE_LINE,

    /*Event as status notifier*/
    DATA_LOADED,

    PROPERTY_CHANGED,
    NAME_CHANGED,
    COLUMN_LIST_CHANGED,

    LINE_REMOVED,
    LINE_ADDED

}
