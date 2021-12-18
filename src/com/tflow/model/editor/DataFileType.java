package com.tflow.model.editor;

import java.util.HashMap;
import java.util.Map;

public enum DataFileType {
    /*TODO: need complete list for Output-File parameters*/

    IN_SQL("SQL File",
            /* parameter-name=data-type:width:default-value */
            "Filename:FileOpen(.sql)",
            "Quotes for name:String=1:\"",
            "Quotes for value:String=1:\""),
    IN_MD("Markdown File",
            "Filename:FileOpen(.md,.txt)"),
    IN_ENV("System Environment",
            "Filename:System"),
    IN_DIR("Directory List",
            "Filename:Dir"),

    OUT_SQL("SQL File",
            /*TODO: how to use DynamicExpression within filename*/
            "Filename:FileSave(.sql)"),
    OUT_MD("Markdown File",
            "Filename:FileSave(.md)"),
    OUT_CSV("CSV File",
            "Filename:FileSave(.csv,.txt)"),
    OUT_TXT("TXT File",
            "Filename:FileSave(.txt)"),
    ;

    private String name;
    private Map<String, String> paramMap;

    DataFileType(String name, String... parameters) {
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
