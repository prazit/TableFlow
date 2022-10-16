package com.tflow.model.editor;

public enum SystemVariable {

    SOURCE_OUTPUT_PATH("description here"),
    TARGET_OUTPUT_PATH("description here"),
    MAPPING_OUTPUT_PATH("description here"),

    SOURCE_FILE_NUMBER("description here"),
    TARGET_FILE_NUMBER("description here"),
    MAPPING_FILE_NUMBER("description here"),

    // Row number will be reset at the beginning of any datatable processes.
    ROW_NUMBER("description here"),

    // Version of current configuration
    CONFIG_VERSION("description here"),

    // Current Version of DConvers.jar (full text)
    APPLICATION_FULL_VERSION("description here"),

    // Current Version of DConvers.jar
    APPLICATION_VERSION("description here"),

    // EXIT CODE for now
    APPLICATION_STATE("description here"),
    APPLICATION_STATE_MESSAGE("description here"),

    // Constant values for configuration
    EMPTY_STRING("description here"),    // ""
    APPLICATION_START("description here"),     // The time to start this dconvers.

    NOW("description here"),                   // Time in realtime.

    // Variables for Table Reader
    TABLE_READER("description here"),
    ROW_READER("description here"),
    ;

    private String description;

    SystemVariable(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
