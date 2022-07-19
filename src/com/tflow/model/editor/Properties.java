package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This Enum use Prototype String like this<br/>
 * <p>
 * 0-Variable-Name:1-Property-Label:2-Property-Type:3-param[,4-param]..[,@Update-ID][,Java-Script;]
 * <br/>.:1-Variable-Name:2-Sub-Variable-Name:3-Property-Label:4-Property-Type:5-param[,6-param]..[,@Update-ID][,Java-Script;]
 * <br/><br/><b>Description:</b>
 * <br/>Variable-Name used for UI Value Binding(ActiveObject[Variable-Name.][Sub-Variable-Name])
 * <br/>Update-ID used to update component after the value is changed.
 * <br/>Java-Script will run at the end of event 'value-changed'
 * </p>
 */
public enum Properties {
    /*TODO: property description is needed*/
    /*TODO: selectable object need lock/unlock status to enable/disable some properties with lock marked*/

    /*TODO: client side: lets call the properties listener after value-changed*/
    TEST_REDEPLOY(
            "version.1.0.0",
            "version.1.0.1"
    ),

    STEP(
            "name:Name:String:refreshStepList();",
            "--:Debug Only:--",
            "id:ID:ReadOnly",
            "zoom:Zoom:ReadOnly",
            ".:activeObject:selectableId:Active Object:ReadOnly",
            "selectableMap:Selectable Map:ReadOnly"
    ),
    STEP_DATA_SOURCE(
            "type:Data Source Type:DATASOURCETYPE",
            "dataSourceId:Data Source:DATASOURCE::type"
    ),
    DATA_BASE(
            "name:Name:String",
            "--:Connection:--",
            "dbms:DBMS:DBMS",
            "url:Connection String:String",
            "driver:Driver:String",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "--:Debug Only:--",
            "startPlug:Start Plug:String"
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
            "idColName:Key Column:Column:id",
            "--: more detail :--",
            "level:Table Level:ReadOnly",
            "connectionCount:Connection Count:ReadOnly"
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
    FX_PARAM(
            "name:Parameter Name:String"
    ),

    INPUT_TXT(
            "type:Type:FileType:in:refreshProperties();",
            "name:File name:String"
    ),
    INPUT_CSV(
            "type:Type:FileType:in:refreshProperties();",
            "name:File name:String"
    ),
    INPUT_SQL(
            "type:Type:FileType:in:refreshProperties();",
            "name:File name:String",
            ".:propertyMap:quotesName:Quotes for name:String:\"",
            ".:propertyMap:quotesValue:Quotes for value:String:\""
    ),
    INPUT_MARKDOWN(
            "type:Type:FileType:in:refreshProperties();",
            "name:File name:String" /*TODO: do this after file structure is completed, change String of name to Upload. //"name:Filename:Upload:md,txt",*/
    ),
    INPUT_ENVIRONMENT(
            "type:Type:FileType:in:refreshProperties();",
            "name:System Environment:System"
    ),
    INPUT_DIRECTORY(
            "type:Type:FileType:in:refreshProperties();",
            "path:Path:String",
            ".:propertyMap:sub:Include sub-directory:Boolean",
            ".:propertyMap:fileOnly:Show file only:Boolean"
    ),

    OUTPUT_TXT(
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
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
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
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
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
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
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "name:File Name:String",
            "path:File Path:String",
            ".:propertyMap:append:Append:Boolean",
            ".:propertyMap:charset:Charset:Charset",
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
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray:;",
            ".:propertyMap:postSQL:Post-SQL:StringArray:;"
    ),
    OUTPUT_DBUPDATE(
            "type:Output Type:FileType:out:refreshProperties();",
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray:;",
            ".:propertyMap:postSQL:Post-SQL:StringArray:;"
    ),

    /*TODO: need complete list for Parameters or Function Prototypes*/
    CFX_LOOKUP(
            "name:Title:String",
            "function:Function:ColumnFunction",
            "--:Source:--",
            ".:propertyMap:sourceTable:Source Table:SourceTable",
            "--:Conditions:--",
            /*TODO: PropertyType for 'Condition' is needed*/
            /*"Condition:Condition(ColumnName==ColumnName(TargetTableLookup))",*/
            /*"Conditions:ConditionList(ColumnName==ColumnName(TargetTableLookup))",*/
            "--:Value:--",
            ".:propertyMap:sourceColumn:Key:Column:sourceTable",
            ".:propertyMap:sourceColumn:MatchKey:Column:sourceTable",
            ".:propertyMap:sourceColumn:Value:Column:sourceTable",
            ".:propertyMap:nullValue:Replace Null:String"
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

        PropertyView property;
        propertyList = new ArrayList<>();
        for (String prototypeString : prototypeList) {
            property = toPropertyView(prototypeString);
            if (property == null) continue;

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

        return new PropertyView();
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
        String[] prototypes = prototypeString.split("[:]");
        PropertyView propView = new PropertyView();
        String[] params = new String[]{};
        int length = prototypes.length;

        if (prototypes[0].equals("--")) {
            /*separator*/
            propView.setType(PropertyType.SEPARATOR);
            propView.setLabel(prototypes[1]);
            propView.setParams(params);
            propertyList.add(propView);
            return null;
        } else if (prototypes[0].equals(".")) {
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
            } else if (params[i].endsWith(";")) {
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
        return propView;
    }

    public void initPropertyMap(Map<String, Object> propertyMap) {
        for (String property : getPrototypeList()) {
            String[] params = property.split("[:]");
            if (params[0].equals(".")) {
                if (!propertyMap.containsKey(params[1]))
                    propertyMap.put(params[1], PropertyType.valueOf(params[4].toUpperCase()).getInitial());
            }
        }
    }

}
