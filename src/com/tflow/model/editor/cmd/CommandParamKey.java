package com.tflow.model.editor.cmd;

public enum CommandParamKey {

    PROJECT,
    HISTORY,
    ACTION,

    DATA_SOURCE,
    DATA_FILE,
    DATA_TABLE,
    TRANSFORM_TABLE,
    COLUMN_FX,
    TABLE_FX,

    COLUMN_FUNCTION,

    DATA_COLUMN,
    TRANSFORM_COLUMN,

    TOWER,
    LINE_LIST,

    DATA_TEST1,
    DATA_TEST2,
    STEP,

    JAVASCRIPT_BUILDER(true);

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
