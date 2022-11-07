package com.tflow.model.data.query;

public enum ColumnType {

    /**
     * Column specified in normal formatted like this: table.column
     */
    NORMAL,

    /**
     * Column specified in normal formatted with alias like this: table.column alias name
     */
    ALIAS,

    /**
     * Column specified in other formatted with/without alias like this:
     * table.column + table.column alias name
     * table.column * (table.column - table.column)
     */
    COMPUTE,
    ;

}
