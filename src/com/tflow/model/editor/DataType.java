package com.tflow.model.editor;

import com.clevel.dconvers.data.DataString;

import java.sql.Types;

public enum DataType {

    STRING("STR", "string.png"),
    INTEGER("INT", "integer.png"),
    DECIMAL("DEC", "decimal.png"),
    DATE("DTE", "date.png"),
    ;

    private String shorten;
    private String image;

    DataType(String shorten, String image) {
        this.shorten = shorten;
        this.image = image;
    }

    public String getShorten() {
        return shorten;
    }

    public String getImage() {
        return image;
    }

    /**
     * @param type value from java.sql.Types
     */
    public static DataType parse(int type) {
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                return STRING;

            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BOOLEAN:
            case Types.BIT:
                return INTEGER;

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                return DECIMAL;

            case Types.DATE:
            case Types.TIMESTAMP:
                return DATE;

            /*case Types.CLOB:
            case Types.NCLOB:*/
            default:
                return null;
        }
    }
}
