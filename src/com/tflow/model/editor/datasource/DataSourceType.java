package com.tflow.model.editor.datasource;

import com.tflow.model.editor.DataFileType;

public enum DataSourceType {

    /*-- TODO: future feature: more DataSourceType
          RESPONSE(JSON,XML)
          KAFKAPRODUCER(JSON,XML,JAVASERIAL)
    */

    DATABASE("database.png", DataFileType.OUT_SQL),
    SFTP("sftp.png", DataFileType.OUT_CSV, DataFileType.OUT_MD, DataFileType.OUT_TXT, DataFileType.OUT_SQL),
    LOCAL("local.png", DataFileType.OUT_CSV, DataFileType.OUT_MD, DataFileType.OUT_TXT, DataFileType.OUT_SQL),
    SYSTEM("environment.png", DataFileType.OUT_CSV, DataFileType.OUT_MD, DataFileType.OUT_TXT, DataFileType.OUT_SQL),
    ;

    DataFileType[] outputDataFileTypes;

    private String image;
    /*-- TODO: future feature: private String fixedFileName;*/

    DataSourceType(String image, DataFileType... outputDataFileTypes) {
        this.image = image;
        this.outputDataFileTypes = outputDataFileTypes;
    }

    public String getImage() {
        return image;
    }
}
