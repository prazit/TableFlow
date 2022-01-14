package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.datasource.Dbms;

import java.util.ArrayList;

public enum PropertyType {

    SEPARATOR(""),

    EXPRESSION(""),

    READONLY(""),
    BOOLEAN(Boolean.FALSE),
    STRING(""),
    STRINGARRAY(new ArrayList<String>()),
    INT(0),

    DBMS(Dbms.ORACLE, true),
    DATASOURCETYPE(DataSourceType.LOCAL, true),
    FILETYPE(DataFileType.IN_MD, true),
    COLUMNFUNCTION(ColumnFunction.LOOKUP, true),
    TABLEFUNCTION(TableFunction.SORT, true),
    SYSTEM(SystemEnvironment.JVM_ENVIRONMENT, true),

    CHARSET(DataCharset.UTF8, true),
    TXTLENGTHMODE(TxtLengthMode.CHARACTER, true),
    TXTFORMAT("", true), /*TODO: do this in xhtml file, show full list of column with format type and width*/

    DBCONNECTION(/*data-base-id*/0, true),
    DBTABLE(/*table-name*/"", true),

    COLUMN(/*column-id(name)*/0),
    SFTP(/*sftp-id*/0, true),

    COLUMNARRAY(/*column-ids(names)*/new ArrayList<Integer>()),

    UPLOAD(""),
    FTPFILE(""),
    ;

    private Object initial;
    boolean isItemList;

    PropertyType(Object initial) {
        this.initial = initial;
        this.isItemList = false;
    }

    PropertyType(Object initial, boolean isItemList) {
        this.initial = initial;
        this.isItemList = isItemList;
    }

    public Object getInitial() {
        return initial;
    }

    public boolean isItemList() {
        return isItemList;
    }

    public boolean equals(String type) {
        return name().equals(type.toUpperCase());
    }

}
