package com.tflow.model.editor;

import java.util.HashMap;
import java.util.Map;

/**
 * This Enum use Prototype String like this<br/>
 * <code style="color:#6666FF">
 * Member-Name=Property-Label:Property-Type:param[,param]..
 * Member-Name used for UI Update/Process
 * </code>
 */
public enum Properties {

    DATA_BASE(
            "name=Name:String",
            "dbms=DBMS:DBMS",
            "url=Connection String:String",
            "driver=Driver:String",
            "user=User:String:20",
            "password=Password:String:20::true",
            "retry=Connection Retry:Int:9:0"
    ),
    SFTP(
            "name=Name:String",
            "host=Host:String",
            "port=Port:String",
            "user=User:String:20",
            "password=Password:String:20::true",
            "retry=Connection Retry:Int:9:0",
            "rootPath=Root Path:String"
    ),
    DATA_FILE(
            "name=File Name:Upload:sql,csv,txt,md",
            "path=File Path:String",
            "type=File Type:FileType:in:name",
            "paramMap=File Properties:FileProb:type"
    ),
    OUTPUT_FILE(
            "type=File Type:FileType:out",
            "name=File Name:String",
            "path=File Path:String",
            "paramMap=File Properties:FileProb:type"
    ),
    FTP_FILE(
            "sftp=FTP Connection:SFTP",
            "ftpName=FTP File Name:FTPFile",
            "name=Local File Name:String",
            "path=Local File Path:String",
            "type=File Type:FileType:in:ftpName",
            "paramMap=File Properties:FileProb:type"
    ),
    FTP_OUTPUT_FILE(
            "sftp=FTP Connection:SFTP",
            "type=File Type:FileType:out",
            "ftpName=FTP File Name:FTPFile",
            "name=Local File Name:String",
            "path=Local File Path:String",
            "paramMap=File Properties:FileProb:type"
    ),
    DATA_TABLE(
            "name=Table Name:String",
            "idColName=Key Column:Column"
    ),
    DATA_COLUMN(
            "type=Data Type:ReadOnly",
            "name=Column Name:String"
    ),
    DATA_OUTPUT(
            "dataSourceType=Data Source Type:DataSourceType",
            "dataSource=Data Source:Child:DATA_SOURCE::dataSourceType",
            "dataFile=Data File:Child:OUTPUT_FILE"
    ),
    TRANSFORM_TABLE(
            "name=Table Name:String",
            "idColName=Key Column:Column"
    ),
    TRANSFORM_COLUMN(
            "type=Data Type:ReadOnly",
            "name=Column Name:String"
    ),
    COLUMN_FX(
            "name=Name:String",
            "function=Function:Function:Column",
            "paramMap=Parameters:FunctionProp:function"
    ),
    TABLE_FX(
            "name=Name:String",
            "function=Function:Function:Table",
            "paramMap=Parameters:FunctionProp:function"
    )
    ;

    private Map<String, String> paramMap;

    Properties(String... properties) {
        paramMap = new HashMap<>();
        for (String property : properties) {
            String[] kv = property.split("[=]");
            paramMap.put(kv[0], kv[1]);
        }
    }

    /**
     * @return Map(Field - Name, Prototype - String)
     */
    public Map<String, String> getParamMap() {
        return paramMap;
    }
}
