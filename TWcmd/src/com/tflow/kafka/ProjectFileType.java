package com.tflow.kafka;

/**
 * Notice: no patterns for Name and prefix, it up to you ^^.
 */
public enum ProjectFileType {

    /*REQUIRE_TYPE_0("root", 0),
    REQUIRE_TYPE_1("project", 1),
    REQUIRE_TYPE_2("step", 2),
    REQUIRE_TYPE_3("data-table", 3),
    REQUIRE_TYPE_4("transform-table", 4),
    REQUIRE_TYPE_9("uploaded,package,generated", 9),*/

    CLIENT_LIST("client-list", 0),
    CLIENT("", 0),

    VERSIONED_LIST("versioned-list", 0),
    VERSIONED("", 0),

    GROUP_LIST("group-list", 0),
    GROUP("", 0),

    PROJECT("", 1),

    UPLOADED_LIST("uploaded-list", 9),
    UPLOADED("", 9),

    PACKAGE_LIST("package-list", 9),
    PACKAGE("", 9),
    PACKAGED("", 9),

    GENERATED_LIST("generated-list", 9),
    GENERATED("", 9),

    ISSUE_LIST("issue-list", 1),

    STEP_LIST("step-list", 1),
    STEP("", 2),

    DB_LIST("db-list", 1),
    DB("", 1),

    SFTP_LIST("sftp-list", 1),
    SFTP("", 1),

    LOCAL_LIST("local-list", 1),
    LOCAL("", 1),

    DS_LIST("", 1),
    DS("", 1),

    VARIABLE_LIST("var-list", 1),
    VARIABLE("", 1),

    DATA_SOURCE_SELECTOR_LIST("data-source-list", 2),
    DATA_SOURCE_SELECTOR("", 2),

    DATA_FILE_LIST("data-file-list", 2),
    DATA_FILE("", 2),

    DATA_TABLE_LIST("data-table-list", 2),
    DATA_TABLE("", 3),
    DATA_COLUMN_LIST("column-list", 3),
    DATA_OUTPUT_LIST("output-list", 3),
    DATA_COLUMN("", 3),
    DATA_OUTPUT("", 3),

    TRANSFORM_TABLE_LIST("transform-table-list", 2),
    TRANSFORM_TABLE("", 4),
    TRANSFORM_COLUMN_LIST("transform-column-list", 4),
    TRANSFORMATION_LIST("transformation-list", 4),
    TRANSFORM_OUTPUT_LIST("transform-output-list", 4),
    TRANSFORM_COLUMN("", 4),
    TRANSFORM_COLUMNFX("", 4),
    TRANSFORMATION("", 4),
    TRANSFORM_OUTPUT("", 4),

    QUERY("", 5),
    QUERY_TABLE_LIST("query-table-list", 5),
    QUERY_TABLE("", 5),
    QUERY_FILTER_LIST("query-fileter-list", 5),
    QUERY_FILTER("", 5),
    QUERY_SORT_LIST("query-sort-list", 5),
    QUERY_SORT("", 5),

    TOWER("", 2),
    FLOOR("", 2),

    LINE_LIST("line-list", 2),
    LINE("", 2),




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
