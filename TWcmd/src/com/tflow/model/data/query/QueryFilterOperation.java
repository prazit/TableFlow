package com.tflow.model.data.query;

public enum QueryFilterOperation {

    EQ("="),
    GT(">"),
    LT("<"),

    NEQ("<>", "!="),
    GE(">="),
    LE("<="),

    IS("IS"),
    IN("IN"),
    IS_NOT("IS NOT"),
    
    LIKE("LIKE"),
    NOT_IN("NOT IN"),
    NOT_LIKE("NOT LIKE"),
    ;

    private String[] operations;

    QueryFilterOperation(String... operations) {
        this.operations = operations;
    }

    public String[] getOperations() {
        return operations;
    }

    public static QueryFilterOperation parse(String operation) {
        for (QueryFilterOperation value : values()) {
            for (String oper : value.getOperations()) {
                if (operation.equals(oper)) {
                    return value;
                }
            }
        }
        return null;
    }
}
