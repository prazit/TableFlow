package com.tflow.model.data;

/**
 * This is copied of com.tflow.model.editor.DataFileType without properties.
 */
public enum DataFileType {

    IN_MARKDOWN,
    IN_SQL,
    IN_SQLDB,
    IN_DIR,
    IN_ENVIRONMENT,

    OUT_MD,
    OUT_CSV,
    OUT_TXT,
    OUT_SQL,

    OUT_INS,
    OUT_UPD
    ;

    public static DataFileType parse(String dataFileType) {
        try {
            return valueOf(dataFileType);
        }catch (Exception ex) {
            return null;
        }
    }

}
