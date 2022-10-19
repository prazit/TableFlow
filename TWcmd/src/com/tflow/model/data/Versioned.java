package com.tflow.model.data;

import java.util.ArrayList;

/**
 * VERSIONED IDs.
 */
public enum Versioned {

    DRIVER_ORACLE(11, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    DRIVER_DB2(12, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    DRIVER_MYSQL(13, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

    /*TODO: need complete list for VERSIONED*/

    /*DConvers Libs*/
    DATA_CONVERSION(21, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_CONFIGURATION(22, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_TEXT(23, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

    /*WebUI Libs*/
    PRIMEFACES(51, "U", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
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
        String driverCode = Versioned.DRIVER_MYSQL.getProjectTypeCodes();

        String codes;
        for (Versioned versioned : values()) {
            codes = versioned.getProjectTypeCodes();
            if (codes.contains(filter) || codes.contains(driverCode)) {
                versionedList.add(versioned);
            }
        }

        return versionedList;
    }
}
