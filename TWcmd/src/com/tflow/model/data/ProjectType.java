package com.tflow.model.data;

/**
 * Notice:
 * Command Line Interface
 * WEB Interface
 */
public enum ProjectType {

    BATCH("B", PackageType.ZIP),

    /*serverless*/
    KAFKA("K", PackageType.ZIP),
    SERVLET("S", PackageType.ZIP),

    /*require web server*/
    WEBUI("U", PackageType.WAR),
    WEBSERVICE("W", PackageType.WAR),
    ;

    String code;
    PackageType packageType;

    ProjectType(String code, PackageType packageType) {
        this.code = code;
        this.packageType = packageType;
    }

    public String getCode() {
        return code;
    }

    public PackageType getPackageType() {
        return packageType;
    }
}
