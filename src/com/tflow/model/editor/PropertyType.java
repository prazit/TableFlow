package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.data.Dbms;

import java.util.ArrayList;
import java.util.HashMap;

public enum PropertyType {
    TOSTRING(null),

    TITLE(null),
    SEPARATOR(null),

    EXPRESSION(""),


    READONLY(""),
    STRING(""),
    PASSWORD(""),
    STRINGARRAY(new ArrayList<String>()),
    DYNAMICVALUE(""),
    PROPERTIES(new HashMap<String, String>()),

    BOOLEAN(Boolean.FALSE, DataType.INTEGER),
    INT(0, DataType.INTEGER),
    NUMBER(0.0d, DataType.DECIMAL),

    DBMS(Dbms.ORACLE_SID, true),
    DATASOURCE(-1, true),
    DATASOURCETYPE(DataSourceType.LOCAL, true),
    DATAFILETYPE(DataFileType.IN_MARKDOWN, true),
    COLUMNFUNCTION(ColumnFunction.LOOKUP, true),
    TABLEFUNCTION(TableFunction.SORT, true),
    SYSTEM(SystemEnvironment.JVM_ENVIRONMENT, true),

    CHARSET(DataCharset.UTF8, true),
    TXTLENGTHMODE(TxtLengthMode.CHARACTER, true),
    TXTFORMAT("", true), /*TODO: do this in xhtml file, show full list of column with format type and width*/

    DBCONNECTION(/*data-base-id*/0, true),
    DBTABLE(/*table-name*/"", true),

    SOURCETABLE(/*table-selectable-id*/ "", true),
    COLUMN(/*column-selectable-id*/"", true),
    SFTP(/*sftp-id*/0, true),

    COLUMNLIST(/*column-ids(names)*/new ArrayList<Integer>()),

    UPLOAD(""),
    FTPFILE(""),
    BUTTON(""),
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
