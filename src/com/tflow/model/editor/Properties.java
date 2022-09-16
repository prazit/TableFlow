package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This Enum use Prototype String like this<br/>
 * <p>
 * 0-Property-Var:1-Property-Label:2-Property-Type:3-param[,4-param]|[,@Update-ID][,Java-Script;][,[x]Enabled-Var][,[]Disabled-Var]
 * <br/>.:1-Property-Var-Parent:2-Property-Var:3-Property-Label:4-Property-Type:5-param[,6-param]|[,@Update-ID][,Java-Script;][,[x]Enabled-Var][,[]Disabled-Var]
 * <br/><br/><b>Description:</b>
 * <br/>Variable-Name used for UI Value Binding(ActiveObject[Variable-Name.][Sub-Variable-Name])
 * <br/>Update-ID used to update component after the value is changed.
 * <br/>Java-Script will run at the end of event 'value-changed'
 * <br/>Enabled-Var use value from variable to enable property at rendered phase (true=enabled,false=disabled).
 * <br/>Disabled-Var use value from variable to disable property at rendered phase (true=disabled,false=enabled).
 * </p>
 */
public enum Properties {

    @Deprecated
    FX_PARAM(
            "name:Parameter Name:String"
    ),

    PACKAGE(
            "==: Package : Built package or deployment package used to create new version of project and can download package to deploy on server you want :==",
            "--: Package Properties :--",
            "name:Name:String|contentWindow.updatePackageList();",
            "buildDate:Build:ReadOnly",
            "builtDate:Built:ReadOnly",
            "--: More Detail :--",
            "id:ID:ReadOnly",
            "complete:Percent Complete:ReadOnly",
            "finished:Finished:ReadOnly",
            "--: tested :--"
    ),

    PROJECT(
            "==: Project : Project of the data-conversion can contains many Table-Flows or known as step inside :==",
            "--: Project Properties :--",
            "name:Name:String|refreshStepList();",
            "--: More Detail :--",
            "id:ID:ReadOnly",
            "activeStepIndex:Active Step Index:ReadOnly",
            "lastUniqueId:Last Unique ID:ReadOnly",
            "lastElementId:Last Element ID:ReadOnly",
            "--: tested :--"
    ),

    STEP(
            "==: Step : Step contains one table flow chart, one process that consume input-data and produce the output-data at the end :==",
            "--: Step Properties :--",
            "name:Name:String|refreshStepList();",
            "--: More Detail :--",
            "id:ID:ReadOnly",
            "--: tested :--"
    ),

    /*TODO: TEST & COMPLETE ALL PROPERTY ONE BY ONE, after tested need to mark TESTED in comment within the property function*/
    STEP_DATA_SOURCE(
            "==: Data Source : Source of input file that linked to it :==",
            "--: Data Source Properties :--",
            "name:Name:String",
            "type:Data Source Type:DATASOURCETYPE|@propertyForm.scrollPanel",
            "dataSourceId:Data Source:DATASOURCE::type|@propertyForm.scrollPanel"
    ),
    DATA_BASE(
            "==: Data Source : Database connection (JDBC) :==",
            "--: Data Source Properties :--",
            "name:Name:String",
            "--:Connection:--",
            "dbms:DBMS:DBMS",
            "url:Connection String:String",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "--: More Detail :--",
            "id:ID:ReadOnly"
    ),
    SFTP(
            "==: Data Source : SFTP/FTP/FTPS Connection information :==",
            "--: Data Source Properties :--",
            "name:Name:String",
            "--:Connection:--",
            "host:Host:String",
            "port:Port:Int:0:9999",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "rootPath:Root Path:String",
            "tmp:Downloaded Path:String",
            "--: More Detail :--",
            "id:ID:ReadOnly"
    ),
    LOCAL_FILE(
            "==: Data Source : Local Directory used for temporary test in standalone environment before change to use SFTP in production environment (just move the link from Local to SFTP) :==",
            "--: Data Source Properties :--",
            "name:Name:String",
            "rootPath:Root Path:String",
            "--: More Detail :--",
            "id:ID:ReadOnly"
    ),
    DATA_TABLE(
            "==: Data Table : Data Table contains input data that extracted from the data-file received from the linked data-source :==",
            "--: Data Table Properties :--",
            "name:Table Name:String",
            "idColName:Key Column:Column:id",
            "--: More Detail :--",
            "id:Table ID:ReadOnly",
            "level:Table Level:ReadOnly",
            "connectionCount:Connection Count:ReadOnly"
    ),
    DATA_COLUMN(
            "==: Column : Column in Data Table contains only the name for referenced from any Dynamic Values :==",
            "--: Column Properties :--",
            "type:Data Type:ReadOnly",
            "name:Column Name:String",
            "--: More Detail :--",
            "id:Column ID:ReadOnly"
    ),
    TRANSFORM_TABLE(
            "==: Transformation Table : Transformation Table used to transfer/transform data from linked source table and apply some transformations at the end of transfer :==",
            "--: Transformation Table Properties :--",
            "name:Table Name:String",
            "idColName:Key Column:Column:id",
            "--: More Detail :--",
            "id:Table ID:ReadOnly",
            "sourceType:Source Table Type:ReadOnly",
            "sourceId:Source Table ID:ReadOnly"
    ),

    TRANSFORM_COLUMN(
            "==: Column : Column in Transformation Table :==",
            "--: Column Properties :--",
            "type:Type:ReadOnly",
            "name:Name:String",
            "--: Value :--",
            "sourceColumnId:Source Column:Column:sourceId|[]useDynamic",
            "useDynamic:Dynamic Value Expression:BOOLEAN|refreshProperties();",
            "dynamicExpression::DynamicValue|[x]useDynamic:[]useFunction",
            "--: More Detail :--",
            "id:ID:ReadOnly"
            /* Value cases:
             * [X] 1. direct transfer : useDynamic = false, value = column name from source-table
             * [X] 2. dynamic value : useDynamic = true, value = custom dynamic value
             * [ ] 3. TODO: future feature - single function helper : useDynamic = true, useFunction = true, value = generated dynamic value
            "useFunction:Function Helper:BOOLEAN::refreshProperties();",
             */
            /*TODO: include properties from specified property name (for selected function)*/
    ),

    INPUT_SYSTEM_ENVIRONMENT(
            "==: Input File (System Environment) : System Environment Data Set :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "name:System Environment:System"
    ),

    INPUT_TXT(
            "==: Input File (TXT) : Text File in Fixed Length Formatted :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "name:File name:String"
    ),
    INPUT_CSV(
            "==: Input File (CSV) : Text File in Comma Separated Values Formatted :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "name:File name:String"
    ),
    INPUT_SQL(
            "==: Input File (SQL) : Text File contains one SQL statement that will sent to Linked Database Connection to create the real Input File back :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "name:File name:String",
            ".:propertyMap:quotesName:Quotes for name:String:\"",
            ".:propertyMap:quotesValue:Quotes for value:String:\""
    ),
    INPUT_MARKDOWN(
            "==: Input File (MD) : Text File contains one or more tables in Markdown Formatted :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "name:File name:String" /*TODO: do this after file structure is completed, change String of name to Upload. //"name:Filename:Upload:md,txt",*/
    ),
    INPUT_DIRECTORY(
            "==: Input File (DIR) : List of file in directory as a table :==",
            "--: File Properties :--",
            "type:Type:DataFileType:in|refreshProperties();",
            "path:Path:String",
            ".:propertyMap:sub:Include sub-directory:Boolean",
            ".:propertyMap:fileOnly:Show file only:Boolean"
    ),

    /*LOCAL(ALL-FILE-TYPES,CUSTOM-NAME), SFTP(ALL-FILE-TYPES,CUSTOM-NAME)*/
    /*RESPONSE(JSON,XML,FIXED-NAME), DATABASE(SQL,FIXED-NAME), KAFKAPRODUCER(JSON,XML,JAVASERIAL,FIXED-NAME)*/
    OUTPUT_TXT(
            "==: Output File (TXT) : Text File in Fixed Length Formatted :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:LOCAL,SFTP",
            "type:Output Type:DataFileType:out|refreshProperties();",
            "name:File Name:String",
            "path:File Path:String",
            ".:propertyMap:append:Append:Boolean",
            ".:propertyMap:charset:Charset:Charset",
            ".:propertyMap:eol:EOL:String",
            ".:propertyMap:eof:EOF:String",
            ".:propertyMap:separator:Separator:String",
            ".:propertyMap:lengthMode:Length Mode:TxtLengthMode",
            ".:propertyMap:dateFormat:Date Format:String",
            ".:propertyMap:dateTimeFormat:DateTime Format:String",
            ".:propertyMap:fillString:String Filler:String",
            ".:propertyMap:fillNumber:Number Filler:String",
            ".:propertyMap:fillDate:Date Filler:String",
            ".:propertyMap:format:Format:TxtFormat"
    ),
    OUTPUT_CSV(
            "==: Output File (CSV) : Text File in Comma Separated Values Formatted :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:LOCAL,SFTP",
            "type:Output Type:DataFileType:out|refreshProperties();",
            "name:File Name:String",
            "path:File Path:String",
            ".:propertyMap:append:Append:Boolean",
            ".:propertyMap:charset:Charset:Charset",
            ".:propertyMap:bof:BOF:String",
            ".:propertyMap:eol:EOL:String",
            ".:propertyMap:eof:EOF:String",
            ".:propertyMap:header:Column Header:Boolean",
            ".:propertyMap:separator:Separator:String",
            ".:propertyMap:lengthMode:Length Mode:TxtLengthMode",
            ".:propertyMap:integerFormat:Integer Format:String",
            ".:propertyMap:decimalFormat:Decimal Format:String",
            ".:propertyMap:dateFormat:Date Format:String",
            ".:propertyMap:dateTimeFormat:DateTime Format:String"
    ),
    OUTPUT_MARKDOWN(
            "==: Output File (MD) : Text File contains one or more tables in Markdown Formatted :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:LOCAL,SFTP",
            "type:Output Type:DataFileType:out|refreshProperties();",
            "name:File Name:String",
            "path:File Path:String",
            "--:Extra Options:--",
            ".:propertyMap:append:Append:Boolean",
            ".:propertyMap:charset:Charset:Charset",
            ".:propertyMap:eol:EOL:String",
            ".:propertyMap:eof:EOF:String",
            ".:propertyMap:showComment:Show File Comment:Boolean",
            ".:propertyMap:showDataSource:With DataSource:Boolean",
            ".:propertyMap:showQuery:With Query:Boolean",
            ".:propertyMap:showTableTitle:Show Table Name:Boolean",
            ".:propertyMap:showRowNumber:Show Row Number:Boolean",
            ".:propertyMap:showFlowChart:Show Flowchart:Boolean",
            ".:propertyMap:showLongFlowChart:Show Long Flowchart:Boolean"
    ),
    OUTPUT_SQL(
            "==: Output File (SQL) : contains list of insert/update/delete statement that can use by another process later :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:LOCAL,SFTP",
            "type:Output Type:DataFileType:out|refreshProperties();",
            "name:File Name:String",
            "path:File Path:String",
            ".:propertyMap:append:Append:Boolean",
            ".:propertyMap:charset:Charset:Charset",
            ".:propertyMap:eol:EOL:String",
            ".:propertyMap:eof:EOF:String",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:tableName:Table Name:String",
            ".:propertyMap:columnArray:Columns:ColumnArray",
            ".:propertyMap:create:Generate Table Creation Script:Boolean",
            ".:propertyMap:insert:Generate SQL Insert:Boolean",
            ".:propertyMap:update:Generate SQL Update:Boolean",
            ".:propertyMap:preSQL:Pre-SQL:StringArray",
            ".:propertyMap:postSQL:Post-SQL:StringArray"
    ),
    OUTPUT_DBINSERT(
            "==: Output File (DB-Insert) : insert each row into specified table using SQL Insert Statement :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:DATABASE",
            "type:Output Type:DataFileType:out|refreshProperties();",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray",
            ".:propertyMap:postSQL:Post-SQL:StringArray"
    ),
    OUTPUT_DBUPDATE(
            "==: Output File (DB-Update) : update each row into specified table using SQL Update Statement :==",
            "--: File Properties :--",
            "dataSourceId:Data Source:DATASOURCE:DATABASE",
            "type:Output Type:DataFileType:out:refreshProperties();",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray",
            ".:propertyMap:postSQL:Post-SQL:StringArray"
    ),

    /*Notice: all below will include into TRANSFORM_COLUMN by flag useFunction and the selected function*/

    CFX_LOOKUP_FIRST_EDITOR(
            "name:Title:String",
            "function:Function:ColumnFunction",
            "--: Source :--",
            ".:propertyMap:sourceTable:Source Table:SourceTable",
            "--: Conditions :--",
            /*TODO: PropertyType for 'Condition' is needed*/
            /*"Condition:Condition(ColumnName==ColumnName(TargetTableLookup))",*/
            /*"Conditions:ConditionList(ColumnName==ColumnName(TargetTableLookup))",*/
            "--: Value :--: This is mockup view for sample flow",
            ".:propertyMap:sourceColumn:Key:Column:sourceTable",
            ".:propertyMap:sourceColumn:MatchKey:Column:sourceTable",
            ".:propertyMap:sourceColumn:Value:Column:sourceTable",
            ".:propertyMap:nullValue:Replace Null:String"
    ),

    /*Notice: columnFx properties below used when expand the column accordion, but when collapse the column accordion will use COLUMN_FUNCTION instead*/
    CFX_LOOKUP(
            "type:Type:ReadOnly",
            "name:Name:String",
            ".:propertyMap:columnId:Value:Column:sourceTable::[]useFunction",
            "[useFunction: Value Function ::@columnId",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "--: Function :--",
            "function:Function:ColumnFunction",
            "--: Lookup Function Arguments :--",
            ".:propertyMap:sourceColumn:Source Table Compare Column:Column:sourceTable",
            ".:propertyMap:lookupTableId:Lookup Table:Table",
            ".:propertyMap:sourceColumn:Lookup Compare Column:Column:propertyMap.lookupTableId",
            ".:propertyMap:sourceColumn:Lookup Value Column:Column:propertyMap.lookupTableId",
            ".:propertyMap:nullValue:Replace Null Value:String",
            "]: Value Function :",
            "useFunction:useFunction:ReadOnly"
    ),

    CFX_GET(
            "type:Type:ReadOnly",
            "name:Name:String",
            ".:propertyMap:columnId:Value:Column:sourceTable::[]useFunction",
            "[useFunction: Value Function ::@columnId",
            "function:Function:ColumnFunction",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "--: Get Function Arguments :--",
            ".:propertyMap:tableId:Table:Table",
            ".:propertyMap:rowIndex:Row Index:Integer",
            ".:propertyMap:columnIndex:Value Column:Column:propertyMap.tableId",
            "]: Value Function :",
            "useFunction:useFunction:ReadOnly"
    ),

    CFX_CONCAT(
            "type:Type:ReadOnly",
            "name:Name:String",
            ".:propertyMap:columnId:Value:Column:sourceTable::[]useFunction",
            "[useFunction: Value Function ::@columnId",
            "function:Function:ColumnFunction",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "--: Concat Function Arguments :--",
            "]: Value Function :",
            "useFunction:useFunction:ReadOnly"
    ),

    CFX_ROWCOUNT(
            "type:Type:ReadOnly",
            "name:Name:String",
            ".:propertyMap:columnId:Value:Column:sourceTable::[]useFunction",
            "[useFunction: Value Function ::@columnId",
            "function:Function:ColumnFunction",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "--: Row Count Function Arguments :--",
            "]: Value Function :",
            "useFunction:useFunction:ReadOnly"
    ),

    DYN_ARG(
            ""
    ),


    DYN_STR(
            ""
    ),

    DYN_INT(
            ""
    ),

    DYN_DEC(
            ""
    ),

    DYN_DTE(
            ""
    ),

    DYN_DTT(
            ""
    ),


    DYN_SYS(
            ""
    ),

    DYN_SRC(
            ""
    ),

    DYN_TAR(
            ""
    ),

    DYN_MAP(
            ""
    ),


    DYN_TXT(
            ""
    ),

    DYN_HTP(
            ""
    ),

    DYN_FTP(
            ""
    ),


    DYN_LUP(
            ""
    ),

    DYN_NON(
            ""
    ),

    DYN_INV(
            ""
    ),


    DYN_VAR(
            ""
    ),


    TFX_FILTER(
            "name:Name:String",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "[useFunction: Specific Function ::@dynamicValue",
            "function:Function:ColumnFunction",
            "--:Filter Arguments:--",
            ".:parameterMap:Conditions:Function:ColumnFunction",
            "]: Specific Function :",
            "useFunction:useFunction:ReadOnly"
    ),

    TFX_SORT(
            "name:Name:String",
            "value:Value:Column:sourceTable::[]useFunction",
            "[: Dynamic Value Expression ::@value",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "[useFunction: Specific Function ::@dynamicValue",
            "function:Function:ColumnFunction",
            "--: Lookup Function Arguments :--",
            "]: Specific Function :",
            "]: Dynamic Value Expression :",
            "useFunction:useFunction:ReadOnly"
    );

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

        PropertyView property;
        propertyList = new ArrayList<>();
        for (String prototypeString : prototypeList) {
            property = toPropertyView(prototypeString);
            /*TODO: remove this comment when all properties are completed // if (property == null) continue;*/
            propertyList.add(property);
        }

        return propertyList;
    }

    public PropertyView getPropertyView(String propertyVar) {
        List<PropertyView> propertyList = getPropertyList();

        for (PropertyView propertyView : propertyList) {
            if (propertyVar.equals(propertyView.getVar()))
                return propertyView;
        }

        LoggerFactory.getLogger(Properties.class).warn(this.name() + ".getPropertyView(propertyVar:" + propertyVar + ") property not found!", new Exception(""));
        return new PropertyView(propertyVar);
    }

    /**
     * List of property of Column Plug (PropertyType == "Column").<br/>
     * Used by endPlugList of ColumnFX.
     */
    public List<PropertyView> getPlugPropertyList() {
        List<PropertyView> propertyList = getPropertyList();
        List<PropertyView> plugPropertyList = new ArrayList<>();
        if (propertyList.size() == 0) return plugPropertyList;

        String var;
        for (PropertyView property : propertyList) {
            PropertyType type = property.getType();
            if (PropertyType.COLUMN == type) plugPropertyList.add(property);
        }

        return plugPropertyList;
    }

    private PropertyView toPropertyView(String prototypeString) {
        String[] parts = prototypeString.split("[|]");
        String[] prototypes = parts[0].split("[:]");
        String[] params = new String[]{};

        Logger log = LoggerFactory.getLogger(Properties.class);
        log.warn("toPropertyView: prototypes={} from prototypeString='{}'", Arrays.toString(prototypes), prototypeString);

        PropertyView propView = new PropertyView();
        String prototypes0 = prototypes[0];
        int length = prototypes.length;
        switch (prototypes0) {
            case "--":
                /*separator*/
                propView.setType(PropertyType.SEPARATOR);
                propView.setLabel(prototypes[1]);
                propView.setParams(params);
                return propView;

            case "==":
                /*title and description*/
                propView.setType(PropertyType.TITLE);
                propView.setVar(prototypes[1]);
                propView.setLabel(prototypes[2]);
                propView.setParams(params);
                return propView;

            case ".":
                /*var with parent*/
                if (length > 5) params = Arrays.copyOfRange(prototypes, 5, length);
                propView.setType(PropertyType.valueOf(prototypes[4].toUpperCase()));
                propView.setLabel(prototypes[3].isEmpty() ? null : prototypes[3]);
                propView.setVar(prototypes[2]);
                propView.setVarParent(prototypes[1]);
                propView.setParams(params);
                break;

            default:
                /*var without parent*/
                if (length > 3) params = Arrays.copyOfRange(prototypes, 3, length);
                propView.setType(PropertyType.valueOf(prototypes[2].toUpperCase()));
                propView.setLabel(prototypes[1].isEmpty() ? null : prototypes[1]);
                propView.setVar(prototypes0);
                propView.setVarParent(null);
                propView.setParams(params);
        }

        /*option*/
        if (parts.length > 1) {
            for (String optional : parts[1].split("[:]")) {
                if (optional.startsWith("@")) {
                    propView.setUpdate(optional.substring(1).replaceAll("[.]", ":"));
                } else if (optional.startsWith("[]")) {
                    propView.setDisableVar(optional.substring(2));
                } else if (optional.startsWith("[x]")) {
                    propView.setEnableVar(optional.substring(3));
                } else if (optional.endsWith(";")) {
                    propView.setJavaScript(optional);
                }
            }
        }

        return propView;
    }

    public String initPropertyMap(Map<String, Object> propertyMap) {
        StringBuilder propertyOrder = new StringBuilder();
        for (String property : getPrototypeList()) {
            String[] params = property.split("[:]");
            if (params[0].equals(".")) {
                if (!propertyMap.containsKey(params[1])) {
                    propertyMap.put(params[1], PropertyType.valueOf(params[4].toUpperCase()).getInitial());
                    propertyOrder.append(",").append(params[1]);
                }
            }
        }
        return propertyOrder.length() > 0 ? propertyOrder.substring(1) : "";
    }

}
