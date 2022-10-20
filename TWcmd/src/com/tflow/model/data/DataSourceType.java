package com.tflow.model.data;

/**
 * Notice: all enum constants copied from com.tflow.model.editor.DataSourceType.
 */
public enum DataSourceType {

    DATABASE,
    SFTP,
    LOCAL,
    FIXED,
    SYSTEM,
    DIR,
    ;

    public static DataSourceType parse(String name) {
        try{
            return valueOf(name.toUpperCase());
        }catch (Exception ex) {
            return null;
        }
    }
}
