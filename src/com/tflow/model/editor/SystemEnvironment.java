package com.tflow.model.editor;

public enum SystemEnvironment {

    /*TODO: Pair from jfxrt.jar need to change to another Pair class*/
    MEMORY_INFORMATION(13, "memory", "memory"),

    VARIABLE_LIST(0, "variable", "var"),

    JVM_ENVIRONMENT(12, "environment", "property"),
    OS_ENVIRONMENT(14, "os_variable", "variable"),
    APPLICATION_ARGUMENT(11, "arg", "index"),

    TABLE_LIST(1, "table_summary", "index"),
    OUTPUT_LIST(2, "output_summary", "id"),
    ;

    private int id;
    private String query;
    private String idColName;

    SystemEnvironment(int id, String query, String idColName) {
        this.id = id;
        this.query = query;
        this.idColName = idColName;
    }

    public int getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getIdColName() {
        return idColName;
    }

    public static SystemEnvironment parse(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}
