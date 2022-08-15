package com.tflow.model.data;

/**
 * VERSIONED IDs.
 */
public enum Versioned {

    DRIVER_ORACLE(""),
    DRIVER_DB2(""),
    DRIVER_MYSQL(""),

    /*TODO: need complete list for VERSIONED* /
    DATA_CONVERSION("BKSUW"),

    APACHE_CONFIGURATION("BKSUW"),
    APACHE_TEXT("BKSUW"),

    PRIMEFACES("U"),
    */

    ;

    String projectTypeCodes;

    Versioned(String projectTypeCodes) {
        this.projectTypeCodes = projectTypeCodes;
    }

    public String getProjectTypeCodes() {
        return projectTypeCodes;
    }
}
