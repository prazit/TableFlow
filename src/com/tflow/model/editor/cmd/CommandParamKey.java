package com.tflow.model.editor.cmd;

public enum CommandParamKey {

    PROJECT,
    HISTORY,

    DATA_SOURCE,
    DATA_FILE,
    DATA_TABLE,
    COLUMN_FX,
    TRANSFORM_TABLE,

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
