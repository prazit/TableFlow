package com.tflow.model.data;

import java.util.ArrayList;

/**
 * VERSIONED IDs.
 */
public enum Versioned {

    DRIVER_ORACLE(11, ""),
    DRIVER_DB2(12, ""),
    DRIVER_MYSQL(13, ""),

    /*TODO: need complete list for VERSIONED*/

    /*DConvers Libs*/
    DATA_CONVERSION(21, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_CONFIGURATION(22, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_TEXT(23, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

    /*WebUI Libs*/
    PRIMEFACES(51, "U"),
    ;

    private String projectTypeCodes;
    private String allowTypes;
    private String allowMimeType;
    private int fileId;

    Versioned(int fileId, String projectTypeCodes, String allowTypes, String allowMimeType) {
        this.fileId = fileId;
        this.projectTypeCodes = projectTypeCodes;
        this.allowTypes = allowTypes;
        this.allowMimeType = allowMimeType;
    }

    Versioned(int fileId, String projectTypeCodes) {
        this.fileId = fileId;
        this.projectTypeCodes = projectTypeCodes;
        this.allowTypes = "";
        this.allowMimeType = "";
    }

    public int getFileId() {
        return fileId;
    }

    public String getProjectTypeCodes() {
        return projectTypeCodes;
    }

    public String getAllowTypes() {
        return allowTypes;
    }

    public String getAllowMimeType() {
        return allowMimeType;
    }

    public static ArrayList<Versioned> getVersionedList(ProjectType projectType) {
        ArrayList<Versioned> versionedList = new ArrayList<>();
        String filter = projectType.getCode();

        for (Versioned versioned : values()) {
            if (versioned.getProjectTypeCodes().contains(filter)) {
                versionedList.add(versioned);
            }
        }

        return versionedList;
    }
}
