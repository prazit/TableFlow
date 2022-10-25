package com.tflow.model.data;

import java.util.ArrayList;

/**
 * VERSIONED IDs.
 * Notice: don't delete all existing enum below to avoid error in Project-Page.
 */
public enum Versioned {

    /*Database Drivers*/
    DRIVER_ORACLE(11, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    DRIVER_DB2(12, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    DRIVER_MYSQL(13, "D", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    TEMPLATE_LOGBACK_XML(14, "D", FileNameExtension.XML.getAllowTypes(), FileNameExtension.XML.getAllowMimeTypes()),

    /*DConvers Libs*/
    DATA_CONVERSION(21, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_BEANUTILS(22, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_CLI(23, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_CODEC(24, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_CONFIGURATION(25, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_LANG(26, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    APACHE_COMMONS_LOGGING(27, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

    LOGBACK_CORE(28, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    LOGBACK_CLASSIC(29, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),
    SLF4J_API(30, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

    JSCH_SFTP(32, "BKSUW", FileNameExtension.JAR.getAllowTypes(), FileNameExtension.JAR.getAllowMimeTypes()),

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
