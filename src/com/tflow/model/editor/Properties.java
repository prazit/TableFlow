package com.tflow.model.editor;

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
    /*TODO= how to use DynamicExpression within filename, may be need specific type = Expression(String)*/

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
    DATA_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column"
    ),
    DATA_COLUMN(
            "type:Data Type:ReadOnly",
            "name:Column Name:String"
    ),
    DATA_OUTPUT(
            /*TODO: replace Child by DataSource Properties*/
            /*TODO: replace Child by DataFile Properties*/
            "dataSourceType:Data Source Type:DataSourceType",
            "dataSource:Data Source:Child:DATA_SOURCE::dataSourceType",
            "dataFile:Data File:Child:OUTPUT_FILE"
    ),
    TRANSFORM_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column"
    ),
    TRANSFORM_COLUMN(
            "type:Data Type:ReadOnly",
            "name:Column Name:String"
    ),
    COLUMN_FX(
            /*TODO: replace FunctionProp by Function Properties*/
            "name:Name:String",
            "function:Function:Function:Column",
            "paramMap:Parameters:FunctionProp:function"
    ),
    TABLE_FX(
            /*TODO: replace FunctionProp by Function Properties*/
            "name:Name:String",
            "function:Function:Function:Table",
            "paramMap:Parameters:FunctionProp:function"
    ),

    INPUT_SQL(
            "dataSourceId:DB Connection:DBConnection",
            "name:Filename:Upload:sql",
            ".:propertyMap:quotesName:Quotes for name:String:\"",
            ".:propertyMap:quotesValue:Quotes for value:String:\""
    ),
    INPUT_MARKDOWN(
            "dataSource:FTP/SFTP:SFTP",
            "name:Filename:Upload:md,txt"
    ),
    INPUT_ENVIRONMENT(
            "name:Environment:System"
    ),
    INPUT_DIRECTORY(
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
            "path:File Path:String"
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
            ".:propertyMap:columnList:Columns:ColumnList",
            ".:propertyMap:create:Generate Table Creation Script:Boolean",
            ".:propertyMap:insert:Generate SQL Insert:Boolean",
            ".:propertyMap:update:Generate SQL Update:Boolean",
            ".:propertyMap:preSQL:Pre-SQL:StringList",
            ".:propertyMap:postSQL:Post-SQL:StringList"
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

    Properties(String... properties) {
        prototypeList = Arrays.asList(properties);
    }

    public List<String> getPrototypeList() {
        return prototypeList;
    }
}
