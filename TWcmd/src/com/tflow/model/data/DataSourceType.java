package com.tflow.model.data;

/**
 * all enum constants copied from com.tflow.model.editor.DataSourceType.
 */
public enum DataSourceType {

    DATABASE,
    SFTP,
    LOCAL,
    SYSTEM,
    ;

    public static DataSourceType parse(String name) {
        try{
            return valueOf(name.toUpperCase());
        }catch (Exception ex) {
            return null;
        }
    }
}
