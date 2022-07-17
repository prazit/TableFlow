package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.datasource.Dbms;

import java.util.ArrayList;

public enum PropertyType {

    SEPARATOR(""),

    EXPRESSION(""),

    READONLY(""),
    BOOLEAN(Boolean.FALSE, DataType.INTEGER),
    STRING(""),
    STRINGARRAY(new ArrayList<String>()),
    INT(0, DataType.INTEGER),

    DBMS(Dbms.ORACLE, true),
    DATASOURCE(-1, true),
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

    SOURCETABLE(/*table-selectable-id*/ "", true),
    COLUMN(/*column-selectable-id*/"", true) /*TODO: COLUMN is special type that have a plug and need specified data-type for the plug*/,
    SFTP(/*sftp-id*/0, true),

    COLUMNARRAY(/*column-ids(names)*/new ArrayList<Integer>()),

    UPLOAD(""),
    FTPFILE(""),
    ;

    private Object initial;
    boolean isItemList;
    private DataType dataType;

    PropertyType(Object initial) {
        this.initial = initial;
        this.isItemList = false;
        this.dataType = DataType.STRING;
    }

    PropertyType(Object initial, boolean isItemList) {
        this.initial = initial;
        this.isItemList = isItemList;
        this.dataType = DataType.STRING;
    }

    PropertyType(Object initial, DataType dataType) {
        this.initial = initial;
        this.isItemList = false;
        this.dataType = dataType;
    }

    public Object getInitial() {
        return initial;
    }

    public boolean isItemList() {
        return isItemList;
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean equals(String type) {
        return name().equals(type.toUpperCase());
    }

}
