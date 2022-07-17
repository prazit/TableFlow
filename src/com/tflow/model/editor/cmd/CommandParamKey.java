package com.tflow.model.editor.cmd;

public enum CommandParamKey {

    STEP,
    ACTION,

    DATA_SOURCE,
    DATA_SOURCE_SELECTOR,
    DATA_SOURCE_SELECTOR_LIST,

    DATA_FILE,
    DATA_FILE_LIST,

    DATA_TABLE,
    DATA_COLUMN,

    TRANSFORM_TABLE,
    TRANSFORM_COLUMN,
    COLUMN_FX,
    TABLE_FX,

    COLUMN_FUNCTION,

    LINE,
    LINE_LIST,

    DATA_TEST1,
    DATA_TEST2;


    boolean optional;

    CommandParamKey() {
    }

    CommandParamKey(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}
