package com.tflow.kafka;

import org.slf4j.LoggerFactory;

public enum ProjectFileType {

    PROJECT("project", 1),
    DB_LIST("db-list", 1),
    SFTP_LIST("sftp-list", 1),
    LOCAL_LIST("local-list", 1),
    STEP_LIST("step-list", 1),
    DB("db", 1),
    SFTP("sftp", 1),
    LOCAL("local", 1),
    STEP("step", 1),
    DATA_TABLE_LIST("data-table-list", 2),
    TOWER("tower", 2),
    FLOOR("floor", 2),
    LINE_LIST("line-list", 2),
    LINE("line", 2),
    DATA_FILE("data-file", 2),
    DATA_TABLE("data-table", 3),
    DATA_COLUMN_LIST("data-column-list", 3),
    DATA_OUTPUT_LIST("data-output-list", 3),
    DATA_COLUMN("data-column", 3),
    TRANSFORM_TABLE("transform-table", 4),
    TRANSFORM_COLUMN_LIST("transform-column-list", 4),
    TRANSFORMATION_LIST("transformation-list", 4),
    TRANSFORM_OUTPUT_LIST("transform-output-list", 4),
    TRANSFORM_COLUMN("transform-column", 4),
    TRANSFORMATION("transformation", 4),
    TRANSFORM_OUTPUT("transform-output", 4),
    ;

    private String key;
    private int requireType;

    ProjectFileType(String key, int requireType) {
        this.key = key;
        this.requireType = requireType;
    }

    public String getKey() {
        return key;
    }

    public int getRequireType() {
        return requireType;
    }

    public static ProjectFileType parse(String name) {
        ProjectFileType projectFileType;

        try {
            name = name.toUpperCase();
            projectFileType = ProjectFileType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            projectFileType = null;
            LoggerFactory.getLogger(ProjectFileType.class).error("ProjectFileType.parse(name:{}) failed!", name, ex);
        }

        return projectFileType;
    }

}
