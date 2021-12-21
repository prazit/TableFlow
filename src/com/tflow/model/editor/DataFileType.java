package com.tflow.model.editor;

import java.util.HashMap;
import java.util.Map;

public enum DataFileType {
    /*TODO: need complete list for Output-File parameters*/

    IN_SQL("SQL", "sql.png",
            /* parameter-name=data-type:width:default-value */
            "DB Connection=DBConnection",
            "Filename=FileOpen(.sql)",
            "Quotes for name=String=1:\"",
            "Quotes for value=String=1:\""),
    IN_MD("Markdown", "markdown.png",
            "Filename=FileOpen(.md,.txt)"),
    IN_ENV("System Environment", "system.png",
            "Filename=System"),
    IN_DIR("Directory List", "dir.png",
            "Filename=Dir"),

    OUT_DBINSERT("Insert", "sql.png",
            "Table=String"),
    OUT_DBUPDATE("Update", "sql.png",
            "Table=String"),
    OUT_SQL("SQL", "sql.png",
            /*TODO= how to use DynamicExpression within filename*/
            "Filename=FileSave(.sql)"),
    OUT_MD("Markdown", "markdown.png",
            "Filename=FileSave(.md)"),
    OUT_CSV("CSV", "csv.png",
            "Filename=FileSave(.csv,.txt)"),
    OUT_TXT("TXT", "txt.png",
            "Filename=FileSave(.txt)"),
    ;

    private String name;
    private String image;
    private Map<String, String> paramMap;

    DataFileType(String name, String image, String... parameters) {
        this.name = name;
        this.image = image;
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

    public String getImage() {
        return image;
    }
}
