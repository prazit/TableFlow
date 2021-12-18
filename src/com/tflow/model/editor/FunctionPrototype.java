package com.tflow.model.editor;

import java.util.HashMap;
import java.util.Map;

public enum FunctionPrototype {
    /*TODO: need complete list for Parameters or Function Prototypes*/

    COL_LOOKUP("Lookup",
            /* parameter-name=data-type(args)*/
            "TargetTableLookup=TableName",
            "Condition=Condition(ColumnName==ColumnName(TargetTableLookup))",
            /*"Conditions=ConditionList(ColumnName==ColumnName(TargetTableLookup))",*/
            "TargetColumnValue=ColumnName(TargetTableLookup)",
            "TargetColumnValueDefault=ColumnType(TargetColumnValue)"),
    COL_GET("GetValue",
            "Table:Table",
            "Row:Row",
            "Column:ColumnName(Table)"),
    COL_CONCAT("Concat"),
    COL_ROWCOUNT("RowCount"),

    TAB_FILTER("Filter"),
    TAB_SORT("Sort");

    private String name;
    private Map<String, String> paramMap;

    FunctionPrototype(String name, String... parameters) {
        this.name = name;
        paramMap = new HashMap<>();
        for (String parameter : parameters) {
            String[] kv = parameter.split("[=]");
            paramMap.put(kv[0], kv[1]);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

}
