package com.tflow.model.editor;

public enum SystemEnvironment {

    APPLICATION_ARGUMENT(11),
    JVM_ENVIRONMENT(12),
    MEMORY_INFORMATION(13),

    VARIABLE_LIST(0),
    TABLE_LIST(1),
    OUTPUT_LIST(2),
    ;

    private int id;

    SystemEnvironment(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SystemEnvironment parse(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}
