# DATA STRUCTURE

Table Flow

----

## COMPATIBLE STRUCTURED

| :heavy_check_mark: | name in TFlow           | Structured in DConvers         | remark            |
|:------------------:| ----------------------- | ------------------------------ | ----------------- |
| :x:                | Batch                   | Conversion Properties          | [Conversion file] |
| :heavy_check_mark: | Steps                   | List of Converter files        | [Conversion file] |
| :heavy_check_mark: | Data Source Connections | List of Data Source Properties | [Conversion file] |
| :heavy_check_mark: | FTP/SFTP Connections    | List of SFTP Properties        | [Conversion file] |
|                    | Project Variables       | List of Variables              | [Conversion file] |
| :heavy_check_mark: | Step                    | Converter Properties           | [Converter file]  |
| :heavy_check_mark: | Data Tables             | List of Source Properties      | [Converter file]  |
| :heavy_check_mark: | Transform Tables        | List of Target Properties      | [Converter file]  |

## ACTIVE WORKSPACE

| :heavy_check_mark: | name    | data type | description                  |
|:------------------:| ------- | --------- | ---------------------------- |
| :heavy_check_mark: | project | Project   | active project               |
| :heavy_check_mark: | user    | User      | active user                  |
| :heavy_check_mark: | client  | Client    | active client (for security) |

## PROJECT

| :heavy_check_mark: | name            | data type          | description                                                                            |
|:------------------:| --------------- | ------------------ | -------------------------------------------------------------------------------------- |
| :heavy_check_mark: | name            | string             |                                                                                        |
| :heavy_check_mark: | batch           | Batch              | [COMPATIBLE STRUCTURED]                                                                |
| :heavy_check_mark: | activeStepIndex | Step.index         | index of step in stepList                                                              |
| :heavy_check_mark: | stepList        | List of Step       |                                                                                        |
| :heavy_check_mark: | dataSourceList  | List of Data Souce |                                                                                        |
| :heavy_check_mark: | sftpList        | List of SFTP       |                                                                                        |
| :heavy_check_mark: | variableList    | List of Variable   |                                                                                        |
| :heavy_check_mark: | lastElementId   | integer            | last generated id for HTML element in this project.<br />elementID = e<lastElementId>; |

## USER

| :heavy_check_mark: | name  | data type | description                                      |
|:------------------:| ----- | --------- | ------------------------------------------------ |
| :heavy_check_mark: | theme | Theme     | [Need to use browser session storage technology] |
|                    | ...   |           | Logged In information are needed here.           |

## STEP

| :heavy_check_mark: | name          | data type               | description                                                                                                                                                                                |
|:------------------:| ------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| :heavy_check_mark: | id            | integer                 | unique number used as ID of the step.<br />This id allow this step able to change the name and index freely but all references to this step need to use this id instead of name and index. |
| :heavy_check_mark: | name          | string                  |                                                                                                                                                                                            |
| :heavy_check_mark: | index         | integer                 | unique number used to sort step.                                                                                                                                                           |
| :heavy_check_mark: | history       | List of Action          | List as stack                                                                                                                                                                              |
| :heavy_check_mark: | dataList      | List of Data Table      |                                                                                                                                                                                            |
| :heavy_check_mark: | transformList | List of Transform Table |                                                                                                                                                                                            |
| :heavy_check_mark: | outputList    | List of Data Output     |                                                                                                                                                                                            |

## TOWER FLOOR ROOM

Only objects below can put in the Room.

+ DataSource (both input and output)
+ DataFile (both input and output)
+ DataTable
+ TransformTable

## ACTION

Action or user-command that make some changes to the active project.

| :heavy_check_mark: | name        | data type       | description                                                                                                                                                                                         |
|:------------------:| ----------- | --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | icon        | string          | CSS classes (space separated values)                                                                                                                                                                |
| :heavy_check_mark: | name        | string          | action name, display name                                                                                                                                                                           |
| :heavy_check_mark: | code        | string          | unique code, old school command, used to take action from command box (comma separated commands) or command file (line break separated commands) [user-command]                                     |
| :heavy_check_mark: | description | string          | sub-display name, long description                                                                                                                                                                  |
| :heavy_check_mark: | canUndo     | boolean         |                                                                                                                                                                                                     |
| :heavy_check_mark: | canRedo     | boolean         |                                                                                                                                                                                                     |
| :heavy_check_mark: | paramMap    | Map<K,V>        | Parameters for all command in the command list.                                                                                                                                                     |
| :heavy_check_mark: | commandList | List of Command | Editor-Command class contains 'do' and 'undo' functions for the action or group of commands. <br /><br /><sub>**Remember:**<br />User Command = Action Class<br />Dev Command = Command Class</sub> |

## DATA SOURCE

ROOM, base class for DATABASE, LOCAL and SFTP

| :heavy_check_mark: | name | data type      | description                                                       |
|:------------------:| ---- | -------------- | ----------------------------------------------------------------- |
| :heavy_check_mark: | type | DataSourceType | [Enum] database, local, ftp/sftp                                  |
| :heavy_check_mark: | id   | int            | unique id depends on the type                                     |
| :heavy_check_mark: | name | String         | Data source name                                                  |
| :heavy_check_mark: | plug | string         | HTML element id used by Linked Line (start-plug), auto generated. |

## DATABASE

Extends from DATA SOURCE

| :heavy_check_mark: | name              | Property                      | Data Type                         | Default Value | Description                                             |
|:------------------:| ----------------- | ----------------------------- | --------------------------------- | ------------- | ------------------------------------------------------- |
| :heavy_check_mark: | dbms              | --                            | DBMS                              | --            | Enumeration                                             |
| :heavy_check_mark: | url               | datasource.url                | string                            | null          | jdbc connection string                                  |
| :heavy_check_mark: | driver            | datasource.driver             | string                            | null          | driver class name with full package location            |
| :heavy_check_mark: | user              | datasource.user               | string                            | null          | user name to connect to DBMS                            |
| :heavy_check_mark: | userEncrypted     | datasource.user.encrypted     | boolean                           | false         | the user name is encrypted                              |
| :heavy_check_mark: | password          | datasource.password           | string                            | null          | password to connect to DBMS                             |
| :heavy_check_mark: | passwordEncrypted | datasource.password.encrypted | boolean                           | false         | the password is encrypted                               |
| :heavy_check_mark: | retry             | datasource.retry              | integer                           | 1             | number of retry when connection is failed               |
| :heavy_check_mark: | quotesForName     | datasource.quotes.name        | string                            | empty         | one character for quotes of string-value and date-value |
| :heavy_check_mark: | quotesForValue    | datasource.quotes.value       | string                            | "             | one character for quotes of string-value and date-value |
| :heavy_check_mark: | propList          | datasource.prop.*             | List of pair<property_name,value> | empty         | list of property sent to DBMS when make a connection.   |

## SFTP

Extends from DATA SOURCE

| :heavy_check_mark: | name        | Property      | Data Type      | Default Value | Description                                                                                                                |
|:------------------:| ----------- | ------------- | -------------- | ------------- | -------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | pathHistory | --            | List of String | --            | List of available path for this SFTP Connection that used to generate the Suggested List                                   |
| :heavy_check_mark: | rootPath    |               | String         |               | allow different root object.                                                                                               |
| :heavy_check_mark: | host        | sftp.host     | string         | null          | ip address or domain name of the SFTP server                                                                               |
| :heavy_check_mark: | port        | sftp.port     | string         | 22            | port use to connect to SFTP server                                                                                         |
| :heavy_check_mark: | user        | sftp.user     | string         | null          | user name to connect to SFTP server                                                                                        |
| :heavy_check_mark: | password    | sftp.password | string         | null          | password to connect to SFTP server                                                                                         |
| :heavy_check_mark: | retry       | sftp.retry    | integer        | 1             | number of retry when connection lost                                                                                       |
| :heavy_check_mark: | tmp         | sftp.tmp      | string         | null          | path to the temporary folder used to store file downloaded from the remote server by FTP function. (Dynamic Value Enabled) |

## LOCAL

Extends from DATA SOURCE

| :heavy_check_mark: | name        | Property | Data Type      | Default Value | Description                                                                              |
|:------------------:| ----------- | -------- | -------------- | ------------- | ---------------------------------------------------------------------------------------- |
| :heavy_check_mark: | pathHistory | --       | List of String | --            | List of available path for this SFTP Connection that used to generate the Suggested List |
| :heavy_check_mark: | rootPath    |          | String         |               | allow different root object.                                                             |

## DATA FILE

ROOM

| :heavy_check_mark: | name      | data type | description                                                                                                                                                  |
|:------------------:| --------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| :heavy_check_mark: | type      | FileType  | [Enumeration] sql-in, sql-out, markdown-in, markdown-out, csv-out, txt-out<br /><br />contain parameter prototype list.                                      |
| :heavy_check_mark: | name      | String    | file name                                                                                                                                                    |
| :heavy_check_mark: | paramMap  | Map<K,V>  | parameter map (more attributes) <br /><br />:x: contains Datasource Query Parameter for Input,<br />:heavy_check_mark: contains Output Properties for Output |
| :heavy_check_mark: | endPlug   | string    | HTML element id used by Linked Line (end-plug), auto generated.                                                                                              |
| :heavy_check_mark: | startPlug | string    | HTML element id used by Linked Line (start-plug), auto generated.                                                                                            |

## DATA TABLE

ROOM

| :heavy_check_mark: | name        | data type           | description                                                                                                                                                                                                                                          |
|:------------------:| ----------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | id          | integer             | unique number used as ID of the table.<br />This id allow this table able to change the name and index freely but all references to this table need to use this id instead of name and index.                                                        |
| :heavy_check_mark: | name        | string              | table name                                                                                                                                                                                                                                           |
| :heavy_check_mark: | index       | integer             | use to sort data-table in the batch process                                                                                                                                                                                                          |
| :heavy_check_mark: | dataFile    | Data File           |                                                                                                                                                                                                                                                      |
| :heavy_check_mark: | dataSource  | Data Source         |                                                                                                                                                                                                                                                      |
| :heavy_check_mark: | query       | string              | A query string is used to retrieve data from a datasource. (Dynamic Value Enabled)<br /><br />Local File: copy file to data folder separated by file-type<br />FTP File: choose SFTP connection, auto create variable for FTP Path, key-in file name |
| :heavy_check_mark: | idColName   | string              | [original name is id] Name of column that contains a key value for a data table of this source.                                                                                                                                                      |
| :heavy_check_mark: | columnList  | List of Data Column | List of column of the Data Table. (Data Column and Transform Column are differenced)                                                                                                                                                                 |
| :heavy_check_mark: | outputList  | List of DataOutput  |                                                                                                                                                                                                                                                      |
| :heavy_check_mark: | noTransform | boolean             | [original name is target] when true this table will be destroyed immediately after all outputs are printed (free up memory)                                                                                                                          |
| :heavy_check_mark: | endPlug     | string              | HTML element id used by Linked Line (end-plug), auto generated.                                                                                                                                                                                      |
| :heavy_check_mark: | startPlug   | string              | HTML element id used by Linked Line (start-plug), auto generated.                                                                                                                                                                                    |

## DATA COLUMN

| :heavy_check_mark: | name      | data type | description                                                       |
|:------------------:| --------- | --------- | ----------------------------------------------------------------- |
| :heavy_check_mark: | type      | string    | data-type load from Data File, allow to change                    |
| :heavy_check_mark: | name      | string    | column-name load from Data File, not allow to change              |
| :heavy_check_mark: | startPlug | string    | HTML element id used by Linked Line (start-plug), auto generated. |

## DATA OUTPUT

| :heavy_check_mark: | name       | data type   | description                                                       |
|:------------------:| ---------- | ----------- | ----------------------------------------------------------------- |
| :heavy_check_mark: | dataFile   | Data File   | output file                                                       |
| :heavy_check_mark: | dataSource | Data Source | output connection                                                 |
| :heavy_check_mark: | startPlug  | string      | HTML element id used by Linked Line (start-plug), auto generated. |

## TRANSFORM TABLE

ROOM

| :heavy_check_mark: | name       | data type                | description                                                                                                                                                                                   |
|:------------------:| ---------- | ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | id         | integer                  | unique number used as ID of the table.<br />This id allow this table able to change the name and index freely but all references to this table need to use this id instead of name and index. |
| :heavy_check_mark: | name       | string                   | table name                                                                                                                                                                                    |
| :heavy_check_mark: | index      | integer                  | use to sort transform-table in the batch process                                                                                                                                              |
| :heavy_check_mark: | dataTable  | Data Table               | source table, all rows in the source table will transferred to this table row by row.                                                                                                         |
| :heavy_check_mark: | idColName  | string                   | [original name is id] Name of column that contains a key value for a data table of this source.                                                                                               |
| :heavy_check_mark: | columnList | List of Transform Column | List of column of the Transform Table. (Data Column and Transform Column are differenced)                                                                                                     |
| :heavy_check_mark: | fxList     | List of TableFx          | [original name is target.transform] List of Transform Functions that run after all rows of the source table are transferred.                                                                  |
| :heavy_check_mark: | outputList | List of DataOutput       |                                                                                                                                                                                               |

## TRANSFORM COLUMN

| :heavy_check_mark: | name        | data type | description                                                |
|:------------------:| ----------- | --------- | ---------------------------------------------------------- |
| :heavy_check_mark: | name        | string    | column name                                                |
| :heavy_check_mark: | type        | string    | data type                                                  |
| :heavy_check_mark: | dataColName | string    | column name (TransformTable.dataTable.colum[name]) or null |
| :heavy_check_mark: | fx          | ColumnFx  | function definition to show in the transform box           |
| :heavy_check_mark: | endPlug     | string    | element id for End of Linked Line                          |
| :heavy_check_mark: | startPlug   | string    | element id for Start of Linked Line                        |

## COLUMN FX

| :heavy_check_mark: | name      | data type         | description                                         |
|:------------------:| --------- | ----------------- | --------------------------------------------------- |
| :heavy_check_mark: | name      | string            | display name, empty string will show the expression |
| :heavy_check_mark: | function  | FunctionPrototype | (Enum) filter by Prefix COL                         |
| :heavy_check_mark: | paramMap  | Map<K,V>          | Parameters for all command in the command list.     |
| :heavy_check_mark: | endPlug   | string            | element id for End of Linked Line                   |
| :heavy_check_mark: | startPlug | string            | element id for Start of Linked Line                 |

## TABLE FX

| :heavy_check_mark: | name     | data type         | description                                         |
|:------------------:| -------- | ----------------- | --------------------------------------------------- |
| :heavy_check_mark: | name     | string            | display name, empty string will show the expression |
| :heavy_check_mark: | function | FunctionPrototype | (Enum) filter by Prefix TAB                         |
| :heavy_check_mark: | paramMap | Map<K,V>          | Parameters for all command in the command list.     |

----

-- end of document --
