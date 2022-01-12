package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Enum use Prototype String like this<br/>
 * <p style:"color:#6666FF">
 * <br/>0-Variable-Name:1-Property-Label:2-Property-Type:3-param[,4-param]..
 * <br/><br/>.:1-Variable-Name:2-Variable-Name:3-Property-Label:4-Property-Type:5-param[,6-param]..
 * <br/><br/>Variable-Name. used for UI Binding(ActiveObject[Variable-Name.][Variable-Name])
 * <br/>Variable-Name used for UI Binding(ActiveObject[Variable])
 * </p>
 */
public enum Properties {
    /*TODO= how to update/redraw property sheet*/
    /*TODO= how to specify some actions/events*/

    DATA_BASE(
            "name:Name:String",
            "dbms:DBMS:DBMS",
            "url:Connection String:String",
            "driver:Driver:String",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0"
    ),
    SFTP(
            "name:Name:String",
            "host:Host:String",
            "port:Port:String",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "rootPath:Root Path:String"
    ),
    LOCAL_FILE(
            "name:Name:String",
            "rootPath:Root Path:String"
    ),
    DATA_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column:id"
    ),
    DATA_COLUMN(
            "type:Data Type:ReadOnly",
            "name:Column Name:String"
    ),
    TRANSFORM_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column:id"
    ),
    TRANSFORM_COLUMN(
            "type:Data Type:ReadOnly",
            "name:Column Name:String"
    ),

    INPUT_SQL(
            "type:Type:FileType:refreshProperties();",
            ".:dataSource:name:DB Connection:DBConnection",
            "name:Filename:String",
            ".:propertyMap:quotesName:Quotes for name:String:\"",
            ".:propertyMap:quotesValue:Quotes for value:String:\""
    ),
    INPUT_MARKDOWN(
            "type:Type:FileType:refreshProperties();",
            "dataSource:FTP/SFTP:SFTP",

            /*TODO: change String of name to Upload. //"name:Filename:Upload:md,txt",*/
            "name:Filename:String"
    ),
    INPUT_ENVIRONMENT(
            "type:Type:FileType:refreshProperties();",
            "name:Environment:System"
    ),
    INPUT_DIRECTORY(
            "type:Type:FileType:refreshProperties();",
            "path:Directory:String"
    ),

    OUTPUT_TXT(
            /*TODO: need complete list for Output properties*/
            "dataSource:FTP/SFTP:SFTP",
            "name:File Name:String",
            "path:File Path:String"
    ),
    OUTPUT_CSV(
            /*TODO: need complete list for Output properties*/
            "dataSource:FTP/SFTP:SFTP",
            "name:File Name:String",
            "path:File Path:String",
            /*TODO: remove all vars below (test data-type)*/
            ".:propertyMap:columnArray:Columns:ColumnArray:owner:id"
    ),
    OUTPUT_MARKDOWN(
            /*TODO: need complete list for Output properties*/
            "dataSource:FTP/SFTP:SFTP",
            "name:File Name:String",
            "path:File Path:String"
    ),
    OUTPUT_SQL(
            "dataSource:FTP/SFTP:SFTP",
            "name:File Name:String",
            "path:File Path:String",
            ".:propertyMap:append:Append:Boolean",  //TODO: Boolean maybe need predefined list of items.
            ".:propertyMap:charset:Charset:String", //TODO: Charset need predefined list of items.
            ".:propertyMap:eol:EOL:String",
            ".:propertyMap:eof:EOF:String",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:tableName:String",
            ".:propertyMap:columnArray:Columns:ColumnArray:,",
            ".:propertyMap:create:Generate Table Creation Script:Boolean",
            ".:propertyMap:insert:Generate SQL Insert:Boolean",
            ".:propertyMap:update:Generate SQL Update:Boolean",
            ".:propertyMap:preSQL:Pre-SQL:StringArray:;",
            ".:propertyMap:postSQL:Post-SQL:StringArray:;"
    ),
    OUTPUT_DBINSERT(
            /*TODO: need complete list for Output properties*/
            /*TODO: need options to log all SQL before execute.*/
            "dataSource:DB Connection:DBConnection",
            ".:propertyMap:dbTable:Table Name:DBTable:propMap[dbConnection]"
    ),
    OUTPUT_DBUPDATE(
            /*TODO: need complete list for Output properties*/
            /*TODO: need options to log all SQL before execute.*/
            "dataSource:DB Connection:DBConnection",
            ".:propertyMap:dbTable:Table Name:DBTable:propMap[dbConnection]"
    ),

    /*TODO: need complete list for Parameters or Function Prototypes*/
    CFX_LOOKUP(
            "name:File Name:String"
            /*"SourceTable:TableName",
            "Condition:Condition(ColumnName==ColumnName(TargetTableLookup))",
            "Conditions:ConditionList(ColumnName==ColumnName(TargetTableLookup))",
            "SourceColumn:ColumnName(TargetTableLookup)",
            "DefaultValue:ColumnType(TargetColumnValue)"*/
    ),
    CFX_GET(
            "name:File Name:String"
            /*"Table:Table",
            "Row:Row",
            "Column:ColumnName(Table)"*/
    ),
    CFX_CONCAT(
            "name:File Name:String"

    ),
    CFX_ROWCOUNT(
            "name:File Name:String"

    ),

    TFX_FILTER(
            "name:File Name:String"

    ),
    TFX_SORT(
            "name:File Name:String"

    ),
    ;

    private List<String> prototypeList;
    private List<PropertyView> propertyList;

    Properties(String... properties) {
        prototypeList = Arrays.asList(properties);
    }

    public List<String> getPrototypeList() {
        return prototypeList;
    }

    public List<PropertyView> getPropertyList() {
        if (propertyList != null) return propertyList;

        propertyList = new ArrayList<>();
        PropertyView propView;
        String[] prototypes;
        String[] params;
        int length;
        for (String prototypeString : prototypeList) {
            prototypes = prototypeString.split("[:]");
            propView = new PropertyView();
            params = new String[]{};
            length = prototypes.length;

            if (prototypes[0].equals(".")) {
                if (length > 5)
                    params = Arrays.copyOfRange(prototypes, 5, length);
                propView.setType(PropertyType.valueOf(prototypes[4].toUpperCase()));
                propView.setLabel(prototypes[3]);
                propView.setVar(prototypes[2]);
                propView.setVarParent(prototypes[1]);
            } else {
                if (length > 3)
                    params = Arrays.copyOfRange(prototypes, 3, length);
                propView.setType(PropertyType.valueOf(prototypes[2].toUpperCase()));
                propView.setLabel(prototypes[1]);
                propView.setVar(prototypes[0]);
                propView.setVarParent(null);
            }

            int paramCount = params.length;
            for (int i = paramCount - 1; i >= 0; i--) {
                if (params[i].contains("@")) {
                    paramCount = i;
                    propView.setUpdate(params[i].substring(1));
                } else if(params[i].endsWith(";")) {
                    paramCount = i;
                    propView.setJavaScript(params[i]);
                }
            }

            if (paramCount > 0) {
                params = Arrays.copyOfRange(params, 0, paramCount);
            } else {
                params = new String[]{};
            }

            propView.setParams(params);
            propertyList.add(propView);
        }
        return propertyList;
    }
}
