# DATA STRUCTURE

Table Flow

----



## COMPATIBLE STRUCTURED

|      | name in TFlow           | Structured in DConvers         | remark            |
| ---- | ----------------------- | ------------------------------ | ----------------- |
|      | Batch                   | Conversion Properties          | [Conversion file] |
|      | Steps                   | List of Converter files        | [Conversion file] |
|      | Data Source Connections | List of Data Source Properties | [Conversion file] |
|      | FTP/SFTP Connections    | List of SFTP Properties        | [Conversion file] |
|      | Project Variables       | List of Variables              | [Conversion file] |
|      | Step                    | Converter Properties           | [Converter file]  |
|      | Data Tables             | List of Source Properties      | [Converter file]  |
|      | Transform Tables        | List of Target Properties      | [Converter file]  |



## ACTIVE WORKSPACE

| :heavy_check_mark: | name    | data type | description                  |
| :----------------: | ------- | --------- | ---------------------------- |
| :heavy_check_mark: | project | Project   | active project               |
| :heavy_check_mark: | user    | User      | active user                  |
| :heavy_check_mark: | client  | Client    | active client (for security) |



## PROJECT

| :heavy_check_mark: | name            | data type          | description                                                  |
| :----------------: | --------------- | ------------------ | ------------------------------------------------------------ |
| :heavy_check_mark: | name            | string             |                                                              |
| :heavy_check_mark: | batch           | Batch              | [COMPATIBLE STRUCTURED]                                      |
| :heavy_check_mark: | activeStepIndex | Step.index         | index of step in stepList                                    |
| :heavy_check_mark: | stepList        | List of Step       |                                                              |
| :heavy_check_mark: | dataSourceList  | List of Data Souce |                                                              |
| :heavy_check_mark: | sftpList        | List of SFTP       |                                                              |
| :heavy_check_mark: | variableList    | List of Variable   |                                                              |
| :heavy_check_mark: | lastElementId   | integer            | last generated id for HTML element in this project.<br />elementID = e<lastElementId>; |



## USER

| :heavy_check_mark: | name  | data type | description                                      |
| ------------------ | ----- | --------- | ------------------------------------------------ |
| :heavy_check_mark: | theme | Theme     | [Need to use browser session storage technology] |
|                    | ...   |           | Logged In information are needed here.           |



## STEP

| :heavy_check_mark: | name               | data type               | description                                                  |
| ------------------ | ------------------ | ----------------------- | ------------------------------------------------------------ |
|                    | id                 | integer                 | unique number used as ID of the step.<br />This id allow this step able to change the name and index freely but all references to this step need to use this id instead of name and index. |
|                    | name               | string                  |                                                              |
|                    | index              | integer                 | unique number used to sort step.                             |
|                    | history            | Stack of Action         | first in last out                                            |
|                    | dataTableList      | List of Data Table      |                                                              |
|                    | transformTableList | List of Transform Table |                                                              |
|                    | outputTableName    | string                  | name of the table that used as Step Output                   |
|                    | outputType         |                         | type of the output used as Step Output                       |
|                    | elementId          | string                  | HTML element id used by Linked Line, auto generated          |



## ACTION

Action or command that make some changes to the active project.

| name        | data type | description                                                  |
| ----------- | --------- | ------------------------------------------------------------ |
| icon        | string    | CSS classes (space separated values)                         |
| name        | string    | action name, display name                                    |
| code        | string    | unique code, old school command, used to take action from command box |
| description | string    | sub-display name, long description                           |
| action      | Class     | action class contains 'do' and 'undo' functions for the action |
| canUndo     | boolean   |                                                              |
| canRedo     | boolean   |                                                              |



## DATA SOURCE BASE

| name      | data type | description                                                  |
| --------- | --------- | ------------------------------------------------------------ |
| type      | String    | [Enum] database, local, ftp/sftp                             |
| name      | String    | Data source name                                             |
| startPlug | string    | HTML element id used by Linked Line (start-plug), auto generated. |



## DATA SOURCE

Extends from DATA SOURCE BASE

| name | data type | description             |
| ---- | --------- | ----------------------- |
| ...  |           | [COMPATIBLE STRUCTURED] |



## SFTP

Extends from DATA SOURCE BASE

| name             | data type      | description                                                  |
| ---------------- | -------------- | ------------------------------------------------------------ |
| ...              |                | [COMPATIBLE STRUCTURED]                                      |
| pathVariableList | List of String | List of available path for this SFTP Connection that used to generate the Suggested List |



## DATA FILE

|      | name      | data type | description                                                  |
| ---- | --------- | --------- | ------------------------------------------------------------ |
|      | type      | String    | [Enumeration] sql, markdown, csv, txt                        |
|      | name      | String    | file name                                                    |
|      | endPlug   | string    | HTML element id used by Linked Line (end-plug), auto generated. |
|      | startPlug | string    | HTML element id used by Linked Line (start-plug), auto generated. |



## DATA TABLE

| name         | data type           | description                                                  |
| ------------ | ------------------- | ------------------------------------------------------------ |
| id           | integer             | unique number used as ID of the table.<br />This id allow this table able to change the name and index freely but all references to this table need to use this id instead of name and index. |
| name         | string              | table name                                                   |
| index        | integer             | use to sort data-table in the batch process                  |
| dataFile     | Data File           |                                                              |
| dataSource   | Data Source         |                                                              |
| query        | string              | A query string is used to retrieve data from a datasource. (Dynamic Value Enabled)<br /><br />Local File: copy file to data folder separated by file-type<br />FTP File: choose SFTP connection, auto create variable for FTP Path, key-in file name |
| idColumnName | string              | [original name is id] Name of column that contains a key value for a data table of this source. |
| columnList   | List of Data Column | List of column of the Data Table. (Data Column and Transform Column are differenced) |
| noTransform  | boolean             | [original name is target] when true this table will be destroyed immediately after all outputs are printed (free up memory) |
| outputList   | List of Output      |                                                              |
| endPlug      | string              | HTML element id used by Linked Line (end-plug), auto generated. |
| startPlug    | string              | HTML element id used by Linked Line (start-plug), auto generated. |



## DATA COLUMN

| name      | data type | description                                                  |
| --------- | --------- | ------------------------------------------------------------ |
| type      | string    | load from Data File, allow to change                         |
| name      | string    | load from Data File, not allow to change                     |
| startPlug | string    | HTML element id used by Linked Line (start-plug), auto generated. |
|           |           |                                                              |



## TRANSFORM TABLE

| name             | data type                | description                                                  |
| ---------------- | ------------------------ | ------------------------------------------------------------ |
| id               | integer                  | unique number used as ID of the table.<br />This id allow this table able to change the name and index freely but all references to this table need to use this id instead of name and index. |
| name             | string                   | table name                                                   |
| index            | integer                  | use to sort transform-table in the batch process             |
| dataTable        | Data Table               | source table, all rows in the source table will transferred to this table row by row. |
| idColumnName     | string                   | [original name is id] Name of column that contains a key value for a data table of this source. |
| columnList       | List of Transform Column | List of column of the Transform Table. (Data Column and Transform Column are differenced) |
| postTranformList | List of Post Transform   | [original name is target.transform] List of Transform Functions that run after all rows of the source table are transferred. |
| outputList       | List of Output           |                                                              |



## TRANSFORM COLUMN

| name        | data type | description                            |
| ----------- | --------- | -------------------------------------- |
| name        | string    | column name                            |
| type        | string    | column type                            |
| query       | string    | (Dynamic Expression) column definition |
| elementId   | string    | HTML element id used by Linked Line    |
| linkStartId | string    | element id for Start of Linked Line    |
| linkEndId   | string    | element id for End of Linked Line      |



----

-- end of document --