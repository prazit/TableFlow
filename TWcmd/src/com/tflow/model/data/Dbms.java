package com.tflow.model.data;

public enum Dbms {

    ORACLE(Versioned.DRIVER_ORACLE, "oracle.jdbc.driver.OracleDriver"),
    DB2(Versioned.DRIVER_DB2, "com.ibm.as400.access.AS400JDBCDriver"),
    MYSQL(Versioned.DRIVER_MYSQL, "com.mysql.jdbc.Driver"),
    ;

    Versioned versioned;
    String driverName;

    Dbms(Versioned versioned, String driverName) {
        this.versioned = versioned;
        this.driverName = driverName;
    }

    public Versioned getDriverFile() {
        return versioned;
    }

    public String getDriverName() {
        return driverName;
    }
}
