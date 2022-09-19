package com.tflow.model.data;

public enum Dbms {

    ORACLE_SID(Versioned.DRIVER_ORACLE, "oracle.png", "oracle.jdbc.driver.OracleDriver", /*jdbc:oracle:thin:@localhost:1521:orcl*/"jdbc:oracle:thin:%s%s%s", "@", ":", ":"),
    ORACLE_SERVICE(Versioned.DRIVER_ORACLE, "oracle.png", "oracle.jdbc.driver.OracleDriver", /*jdbc:oracle:thin:@//localhost:1521/orcl*/"jdbc:oracle:thin:%s%s%s", "@//", ":", "/"),
    DB2(Versioned.DRIVER_DB2, "db2.png", "com.ibm.as400.access.AS400JDBCDriver", /*jdbc:db2://localhost:2222/account*/"jdbc:db2:%s%s%s", "//", ":", "/"),
    MYSQL(Versioned.DRIVER_MYSQL, "mysql.png", "com.mysql.jdbc.Driver", /*jdbc:mysql://localhost:3306/localregistry*/"jdbc:mysql:%s%s%s", "//", ":", "/"),
    MARIA_DB(Versioned.DRIVER_MYSQL, "mariadb.png", "com.mysql.jdbc.Driver", /*jdbc:mysql://localhost:3306/localregistry*/"jdbc:mysql:%s%s%s", "//", ":", "/"),
    ;

    private String image;

    private String hostPrefix;
    private String portPrefix;
    private String schemaPrefix;

    private Versioned versioned;
    private String driverName;
    private String urlPattern;

    Dbms(Versioned versioned, String image, String driverName, String urlPattern, String hostPrefix, String portPrefix, String schemaPrefix) {
        this.versioned = versioned;
        this.image = image;
        this.driverName = driverName;
        this.urlPattern = urlPattern;
        this.hostPrefix = hostPrefix;
        this.portPrefix = portPrefix;
        this.schemaPrefix = schemaPrefix;
    }

    public String getImage() {
        return image;
    }

    public Versioned getDriverFile() {
        return versioned;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getHostPrefix() {
        return hostPrefix;
    }

    public String getPortPrefix() {
        return portPrefix;
    }

    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    public Versioned getVersioned() {
        return versioned;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public String getURL(String host, String port, String schema) {
        return String.format(urlPattern,
                host == null || host.isEmpty() ? "" : hostPrefix + host.trim(),
                port == null || port.isEmpty() ? "" : portPrefix + port.trim(),
                schema == null || schema.isEmpty() ? "" : schemaPrefix + schema.trim()
        );
    }
}
