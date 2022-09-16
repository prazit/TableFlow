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
 * 0-Property-Var:1-Property-Label:2-Property-Type:3-param[,4-param]..[,@Update-ID][,Java-Script;][,[x]Enabled-Var][,[]Disabled-Var]
 * <br/>.:1-Property-Var-Parent:2-Property-Var:3-Property-Label:4-Property-Type:5-param[,6-param]..[,@Update-ID][,Java-Script;][,[x]Enabled-Var][,[]Disabled-Var]
 * <br/><br/><b>Description:</b>
 * <br/>Variable-Name used for UI Value Binding(ActiveObject[Variable-Name.][Sub-Variable-Name])
 * <br/>Update-ID used to update component after the value is changed.
 * <br/>Java-Script will run at the end of event 'value-changed'
 * <br/>Enabled-Var use value from variable to enable property at rendered phase (true=enabled,false=disabled).
 * <br/>Disabled-Var use value from variable to disable property at rendered phase (true=disabled,false=enabled).
 * </p>
 */
public enum Properties {
    /*TODO: property description is defined in the PropertyDesc enum, assigned by description-key = propertyVar*/
    /*TODO: selectable object need lock/unlock status to enable/disable some properties with lock marked*/

    TEST_REDEPLOY(
            "version.1.0.0",
            "version.1.0.1"
    ),
    PACKAGE(
            "name:Name:String",
            "--:Debug Only:--",
            "id:ID:ReadOnly",
            "buildDate:Build:ReadOnly",
            "builtDate:Built:ReadOnly",
            "complete:Percent Complete:ReadOnly"
    ),
    PROJECT(
            "name:Name:String:refreshStepList();",
            "--:Debug Only:--",
            "id:ID:ReadOnly",
            "activeStepIndex:Active Step Index:ReadOnly",
            "lastUniqueId:Last Unique ID:ReadOnly",
            "lastElementId:Last Element ID:ReadOnly"
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
            "name:Name:String",
            "type:Data Source Type:DATASOURCETYPE:@propertyForm.scrollPanel",
            "dataSourceId:Data Source:DATASOURCE::type:@propertyForm.scrollPanel"
    ),
    DATA_BASE(
            "name:Name:String",
            "--:Connection:--",
            "dbms:DBMS:DBMS",
            "url:Connection String:String",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "--:Debug Only:--",
            "id:ID:ReadOnly"
    ),
    SFTP(
            "name:Name:String",
            "--:Connection:--",
            "host:Host:String",
            "port:Port:Int:0:9999",
            "user:User:String:20",
            "password:Password:String:20::true",
            "retry:Connection Retry:Int:9:0",
            "rootPath:Root Path:String",
            "tmp:Downloaded Path:String",
            "--:Debug Only:--",
            "id:ID:ReadOnly"
    ),
    LOCAL_FILE(
            "name:Name:String",
            "rootPath:Root Path:String",
            "--:Debug Only:--",
            "id:ID:ReadOnly"
    ),
    DATA_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column:id",
            "--: more detail :--",
            "id:Table ID:ReadOnly",
            "level:Table Level:ReadOnly",
            "connectionCount:Connection Count:ReadOnly"
    ),
    DATA_COLUMN(
            "type:Data Type:ReadOnly",
            "name:Column Name:String",
            "--: more detail :--",
            "id:Column ID:ReadOnly"
    ),
    TRANSFORM_TABLE(
            "name:Table Name:String",
            "idColName:Key Column:Column:id",
            "--: more detail :--",
            "id:Table ID:ReadOnly",
            "sourceType:Source Table Type:ReadOnly",
            "sourceId:Source Table ID:ReadOnly"
    ),


    /*TODO: need to support new option box syntax defined in the comment below*/
            /*-- Value case 3: --
            ".:propertyMap:value:Value:Column:sourceTable::[]useFunction",
            "[useDynamic: Dynamic Value Expression ::@value",
            ".:propertyMap:dynamicValue:Dynamic Value Expression:DynamicValue",
            "[useFunction: Specific Function ::@dynamicValue",
            "function:Function:ColumnFunction",
            "--:Lookup Function Arguments:--",
            "]: Specific Function :",
            "]: Dynamic Value Expression :"
            */
    TRANSFORM_COLUMN( /*direct transfer without function*/
            "type:Type:ReadOnly",
            "name:Name:String",
            "--: Value :--",
            "sourceColumnId:Source Column:Column:sourceId::[]useDynamic",
            "useDynamic:Dynamic Value Expression:BOOLEAN::refreshProperties();",
            "dynamicExpression:Expression:DynamicValue::[x]useDynamic",
            "--: Debug Only :--",
            "id:ID:ReadOnly"
            /* Value cases:
             * [ ] 1. direct transfer : useDynamic = false, value = column name from source-table
             * [ ] 2. dynamic value : useDynamic = true, value = custom dynamic value
             * [ ] 3. TODO: future feature - single function value : useDynamic = true, useFunction = true, value = generated dynamic value
             **/
    ),
    FX_PARAM(
            "name:Parameter Name:String"
    ),

    INPUT_SYSTEM_ENVIRONMENT(
            "type:Type:DataFileType:in:refreshProperties();",
            "name:System Environment:System"
    ),

    INPUT_TXT(
            "type:Type:DataFileType:in:refreshProperties();",
            "name:File name:String"
    ),
    INPUT_CSV(
            "type:Type:DataFileType:in:refreshProperties();",
            "name:File name:String"
    ),
    INPUT_SQL(
            "type:Type:DataFileType:in:refreshProperties();",
            "name:File name:String",
            ".:propertyMap:quotesName:Quotes for name:String:\"",
            ".:propertyMap:quotesValue:Quotes for value:String:\""
    ),
    INPUT_MARKDOWN(
            "type:Type:DataFileType:in:refreshProperties();",
            "name:File name:String" /*TODO: do this after file structure is completed, change String of name to Upload. //"name:Filename:Upload:md,txt",*/
    ),
    INPUT_DIRECTORY(
            "type:Type:DataFileType:in:refreshProperties();",
            "path:Path:String",
            ".:propertyMap:sub:Include sub-directory:Boolean",
            ".:propertyMap:fileOnly:Show file only:Boolean"
    ),

    OUTPUT_TXT(
            /*TODO: Question: how to disable field 'name' for Data Sources with Fixed file name
             *      Answer: same answer of the question how to retrieve dependency field list */
            /*LOCAL(ALL-FILE-TYPES,CUSTOM-NAME), SFTP(ALL-FILE-TYPES,CUSTOM-NAME)*/
            /*RESPONSE(JSON,XML,FIXED-NAME), DATABASE(SQL,FIXED-NAME), KAFKAPRODUCER(JSON,XML,JAVASERIAL,FIXED-NAME)*/
            "dataSourceId:Data Source:DATASOURCE::dataSourceType:@type",
            "type:Output Type:DataFileType:out:refreshProperties();",
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
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "type:Output Type:DataFileType:out:refreshProperties();",
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
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "type:Output Type:DataFileType:out:refreshProperties();",
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
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "type:Output Type:DataFileType:out:refreshProperties();",
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
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "type:Output Type:DataFileType:out:refreshProperties();",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray:;",
            ".:propertyMap:postSQL:Post-SQL:StringArray:;"
    ),
    OUTPUT_DBUPDATE(
            "dataSourceId:Data Source:DATASOURCE::dataSourceType",
            "type:Output Type:DataFileType:out:refreshProperties();",
            ".:propertyMap:dbTable:Table Name:DBTable:dataSource",
            ".:propertyMap:columnList:Column List:ColumnList",
            ".:propertyMap:quotesOfName:Quotes for Name:String",
            ".:propertyMap:quotesOfValue:Quotes for Value:String",
            ".:propertyMap:preSQL:Pre-SQL:StringArray:;",
            ".:propertyMap:postSQL:Post-SQL:StringArray:;"
    ),

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

    /*TODO: need complete list for Parameters or All Function Prototypes below*/

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

        LoggerFactory.getLogger(Properties.class).error(this.name() + ".getPropertyView(propertyVar:" + propertyVar + ") property not found!", new Exception(""));
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
        String[] prototypes = prototypeString.split("[:]");
        PropertyView propView = new PropertyView();
        String[] params = new String[]{};
        int length = prototypes.length;

        Logger log = LoggerFactory.getLogger(Properties.class);
        log.warn("toPropertyView: prototypes={} from prototypeString='{}'", Arrays.toString(prototypes), prototypeString);

        String prototypes0 = prototypes[0];
        if (prototypes0.equals("--")) {
            /*separator*/
            propView.setType(PropertyType.SEPARATOR);
            propView.setLabel(prototypes[1]);
            propView.setParams(params);
            propertyList.add(propView);
            return null;
        } else if (prototypes0.equals(".")) {
            /*var with parent*/
            if (length > 5)
                params = Arrays.copyOfRange(prototypes, 5, length);
            propView.setType(PropertyType.valueOf(prototypes[4].toUpperCase()));
            propView.setLabel(prototypes[3]);
            propView.setVar(prototypes[2]);
            propView.setVarParent(prototypes[1]);
        } else {
            /*var without parent*/
            if (length > 3)
                params = Arrays.copyOfRange(prototypes, 3, length);
            propView.setType(PropertyType.valueOf(prototypes[2].toUpperCase()));
            propView.setLabel(prototypes[1]);
            propView.setVar(prototypes0);
            propView.setVarParent(null);
        }

        int paramCount = params.length;
        for (int i = paramCount - 1; i >= 0; i--) {
            String parami = params[i];
            if (parami.startsWith("@")) {
                paramCount = i;
                propView.setUpdate(parami.substring(1).replaceAll("[.]", ":"));
            } else if (parami.endsWith(";")) {
                paramCount = i;
                propView.setJavaScript(parami);
            } else if (parami.startsWith("[]")) {
                paramCount = i;
                propView.setDisableVar(parami.substring(2));
            } else if (parami.startsWith("[x]")) {
                paramCount = i;
                propView.setEnableVar(parami.substring(3));
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
