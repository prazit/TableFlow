package com.tflow.model.data;

/**
 * This is copied of com.tflow.model.editor.DataFileType without properties.
 */
public enum DataFileType {

    IN_SQL,
    IN_MD,
    IN_ENV,
    IN_DIR,

    OUT_SQL,
    OUT_MD,
    OUT_CSV,
    OUT_TXT,
    ;

    public static DataFileType parse(String dataFileType) {
        try {
            return valueOf(dataFileType);
        }catch (Exception ex) {
            return null;
        }
    }

}
