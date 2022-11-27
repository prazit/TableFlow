package com.tflow.model.editor.cmd;

public enum CommandParamKey {

    WORKSPACE,
    PROJECT,
    DATA_MANAGER,

    STEP,
    ACTION,
    SELECTABLE,
    PROPERTY,
    DATA,
    PROJECT_FILE_TYPE,

    DATA_SOURCE,
    DATA_SOURCE_SELECTOR,
    DATA_SOURCE_SELECTOR_LIST,

    DATA_FILE,
    DATA_FILE_LIST,

    OUTPUT_FILE,
    OUTPUT_FILE_LIST,

    DATA_TABLE,
    DATA_COLUMN,

    TRANSFORM_TABLE,
    NAMEVALUE_LIST,
    TRANSFORM_COLUMN,
    COLUMN_FX,
    TABLE_FX,

    COLUMN_FUNCTION,

    LINE,
    LINE_LIST,

    DATA_TEST1,
    DATA_TEST2,

    INDEX,
    VIEW_MANAGER,
    VIEW_MAPPER,

    GROUP_ID,
    TEMPLATE_ID,

    BINARY_FILE,
    SWITCH_ON, QUERY, QUERY_COLUMN, COLUMN_ID, QUERY_TABLE;

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
