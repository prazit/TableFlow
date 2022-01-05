package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.datasource.Dbms;

import java.util.ArrayList;

public enum PropertyType {

    READONLY(""),
    BOOLEAN(Boolean.FALSE),
    STRING(""),
    INT(0),

    DBMS(Dbms.ORACLE),
    DATASOURCETYPE(DataSourceType.LOCAL),
    FILETYPE(DataFileType.IN_MD),
    FUNCTION(ColumnFunction.LOOKUP),

    DBCONNECTION(/*data-base-id*/0),
    DBTABLE(/*table-name*/""),

    COLUMN(/*column-id*/0),
    SFTP(/*sftp-id*/0),
    /*CHILD() //TODO: may be need to remove CHILD and put all property of CHILD directly,*/

    STRINGLIST(new ArrayList<String>()),
    COLUMNLIST(/*column-ids*/new ArrayList<Integer>()),

    UPLOAD(""),
    FTPFILE(""),

    /*FUNCTIONPROP() //TODO: similar to CHILD above,*/
    /*FILEPROP(), //TODO: similar to CHILD above*/;

    private Object initial;

    PropertyType(Object initial) {
        this.initial = initial;
    }

    public Object getInitial() {
        return initial;
    }

    public boolean equals(String type) {
        return name().equals(type.toUpperCase());
    }

}
