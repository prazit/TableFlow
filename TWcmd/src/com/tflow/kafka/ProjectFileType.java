package com.tflow.kafka;

/**
 * IMPORTANT: no patterns for Name and prefix, it up to you ^^.
 */
public enum ProjectFileType {

    TEST_TYPE_1("type-1-", 1),
    TEST_TYPE_2("type-2-", 2),
    TEST_TYPE_3("type-3-", 3),
    TEST_TYPE_4("type-4-", 4),

    PROJECT("", 1),
    DB_LIST("db-list", 1),
    SFTP_LIST("sftp-list", 1),
    LOCAL_LIST("local-list", 1),
    DS_LIST("", 1),
    VARIABLE_LIST("var-list", 1),
    STEP_LIST("step-list", 1),
    DB("", 1),
    SFTP("", 1),
    LOCAL("", 1),
    DS("", 1),
    VARIABLE("", 1),

    STEP("", 2),
    DATA_SOURCE_SELECTOR("", 2),
    DATA_SOURCE_SELECTOR_LIST("data-source-list", 2),
    DATA_TABLE_LIST("data-table-list", 2),
    TRANSFORM_TABLE_LIST("transform-table-list", 2),
    TOWER("", 2),
    FLOOR("", 2),
    LINE_LIST("line-list", 2),
    LINE("", 2),
    DATA_FILE("", 2),

    DATA_TABLE("", 3),
    DATA_COLUMN_LIST("column-list", 3),
    DATA_OUTPUT_LIST("output-list", 3),
    DATA_COLUMN("", 3),
    DATA_OUTPUT("", 3),

    TRANSFORM_TABLE("", 4),
    TRANSFORM_COLUMN_LIST("transform-column-list", 4),
    TRANSFORMATION_LIST("transformation-list", 4),
    TRANSFORM_OUTPUT_LIST("transform-output-list", 4),
    TRANSFORM_COLUMN("", 4),
    TRANSFORM_COLUMNFX("", 4),
    TRANSFORMATION("", 4),
    TRANSFORM_OUTPUT("", 4),
    ;

    private String prefix;
    private int requireType;

    ProjectFileType(String prefix, int requireType) {
        this.prefix = prefix;
        this.requireType = requireType;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getRequireType() {
        return requireType;
    }

    @Override
    public String toString() {
        return name();
    }

    public boolean isMe(String name) {
        return (name().compareTo(name) == 0);
    }
}
