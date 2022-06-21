package com.tflow.kafka;

/**
 * IMPORTANT: no patterns for Name and prefix, it up to you ^^.
 */
public enum ProjectFileType {

    TEST_TYPE_1("type-1-", 1),
    TEST_TYPE_2("type-2-", 2),
    TEST_TYPE_3("type-3-", 3),
    TEST_TYPE_4("type-4-", 4),

    PROJECT("proj-", 1),
    DB_LIST("db-list", 1),
    SFTP_LIST("sftp-list", 1),
    LOCAL_LIST("local-list", 1),
    STEP_LIST("step-list", 1),
    DB("db-", 1),
    SFTP("sftp-", 1),
    LOCAL("local-", 1),
    STEP("step-", 1),
    DATA_TABLE_LIST("data-table-list", 2),
    TOWER("tower-", 2),
    FLOOR("floor-", 2),
    LINE_LIST("line-list", 2),
    LINE("line-", 2),
    DATA_FILE("file-", 2),
    DATA_TABLE("table-", 3),
    DATA_COLUMN_LIST("column-list", 3),
    DATA_OUTPUT_LIST("output-list", 3),
    DATA_COLUMN("column-", 3),
    DATA_OUTPUT("output-", 3),
    TRANSFORM_TABLE("transform-", 4),
    TRANSFORM_COLUMN_LIST("transform-column-list", 4),
    TRANSFORMATION_LIST("transformation-list", 4),
    TRANSFORM_OUTPUT_LIST("transform-output-list", 4),
    TRANSFORM_COLUMN("transform-column-", 4),
    TRANSFORMATION("transformation-", 4),
    TRANSFORM_OUTPUT("transform-output-", 4),
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
        return "{" +
                "name: '" + name() + '\'' +
                ", key: '" + prefix + '\'' +
                ", requireType: " + requireType +
                '}';
    }
}
