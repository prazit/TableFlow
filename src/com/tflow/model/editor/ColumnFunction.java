package com.tflow.model.editor;

public enum ColumnFunction {

    /* all constants in this group compatible to DConvers.CalcTypes */
    LOOKUP("CAL:Lookup", Properties.CFX_LOOKUP, true),
    GET("CAL:Get Value", Properties.CFX_GET, true),
    CONCAT("CAL:Concatenation", Properties.CFX_CONCAT, true),
    ROWCOUNT("CAL:Row Count", Properties.CFX_ROWCOUNT, true),
    TRANSFER("CAL:Direct Transfer", Properties.TRANSFORM_COLUMN, true),

    /* all constants below compatible to DConvers.DynamicValueType */
    ARG("ARG:Application Argument", Properties.DYN_ARG, false),

    STR("STR:String Constant", Properties.DYN_STR, false),
    INT("INT:Integer Constant", Properties.DYN_INT, false),
    DEC("DEC:", Properties.DYN_DEC, false),
    DTE("DTE:", Properties.DYN_DTE, false),
    DTT("DTT:", Properties.DYN_DTT, false),

    SYS("SYS:", Properties.DYN_SYS, false),
    SRC("SRC:", Properties.DYN_SRC, false),
    TAR("TAR:", Properties.DYN_TAR, false),
    MAP("MAP:", Properties.DYN_MAP, false),

    TXT("TXT:Text from file", Properties.DYN_TXT, false),
    HTP("HTP:", Properties.DYN_HTP, false),
    FTP("FTP:", Properties.DYN_FTP, false),

    LUP("LUP:", Properties.DYN_LUP, false),
    NON("NON:", Properties.DYN_NON, false),
    INV("INV:", Properties.DYN_INV, false),

    VAR("VAR:", Properties.DYN_VAR, false),
    ;

    private boolean calc;
    private String name;
    private Properties properties;

    ColumnFunction(String name, Properties properties, boolean isCalc) {
        this.name = name;
        this.properties = properties;
        calc = isCalc;
    }

    public String getName() {
        return name;
    }

    public boolean isCalc() {
        return calc;
    }

    public Properties getProperties() {
        return properties;
    }

}
