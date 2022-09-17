# UIs

Table Flow v.0.0.0

----

## APPLICATION FRAME

| :heavy_check_mark: | section     |                       | remark                                  |
|:------------------:| ----------- | --------------------- | --------------------------------------- |
| :heavy_check_mark: | top section |                       | cover logo and menu                     |
| :heavy_check_mark: |             | company logo and name | left side of the top section            |
| :heavy_check_mark: |             | menu bar              | right side of the top section, dropdown |
| :heavy_check_mark: | screen area |                       | workspace for any screen content        |

## TFLOW EDITOR SCREEN

| :heavy_check_mark: | section       |                      | remark                                                                                                                                                                                                                        |
|:------------------:| ------------- | -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | Workspace     |                      | Cover all sections of editor (screen area)                                                                                                                                                                                    |
| :heavy_check_mark: | Step List     |                      | **left side panel**                                                                                                                                                                                                           |
| :heavy_check_mark: | TFlow Area    |                      |                                                                                                                                                                                                                               |
| :heavy_check_mark: |               | Data Table Area      | Data Source<br />Data Table<br />Data Table Output                                                                                                                                                                            |
| :heavy_check_mark: |               | Transform Table Area | Transform Table<br />Transform Output                                                                                                                                                                                         |
| :heavy_check_mark: |               | Step Output Area     | Table Output                                                                                                                                                                                                                  |
| :heavy_check_mark: | Property List |                      | **right side panel**<br /><br />Project Properties<br />Step Properties<br />Database Connection Properties<br />SFTP Connection Properties<br />Data Table Properties<br />Transform Table Properties<br />Output Properties |

### DATA SOURCE

| :heavy_check_mark: |           | section          | remark                                               |
|:------------------:| --------- | ---------------- | ---------------------------------------------------- |
| :heavy_check_mark: | Title bar |                  |                                                      |
| :heavy_check_mark: |           | data source name |                                                      |
| :heavy_check_mark: | Icon Area |                  |                                                      |
| :heavy_check_mark: |           | icon             | data source type (database, local file, sftp, https) |
| :heavy_check_mark: |           | type name        |                                                      |
| :heavy_check_mark: |           | start plug       | ling to data-table                                   |

### DATA TABLE

| :heavy_check_mark: | Section       |             | remark                                                      |
|:------------------:| ------------- | ----------- | ----------------------------------------------------------- |
| :heavy_check_mark: | Title Bar     |             |                                                             |
| :heavy_check_mark: |               | end plug    | link from data source (database, local file, sftp, https)   |
| :heavy_check_mark: |               | icon        | source type                                                 |
| :heavy_check_mark: |               | table name  |                                                             |
| :heavy_check_mark: |               | start plug  | link to transform table                                     |
| :heavy_check_mark: | Column (List) |             |                                                             |
| :heavy_check_mark: |               | icon        | data type                                                   |
| :heavy_check_mark: |               | column name |                                                             |
| :heavy_check_mark: |               | start plug  | link to another table                                       |
| :heavy_check_mark: | Output (List) |             |                                                             |
| :heavy_check_mark: |               | separator   | gap between column list and output list                     |
| :heavy_check_mark: |               | icon        | outup type                                                  |
| :heavy_check_mark: |               | file name   | table name or file name                                     |
| :heavy_check_mark: |               | start plug  | link to Step Output when this output defined as Step Output |

### TRANSFORM TABLE

| :heavy_check_mark: | Section          |               | remark                                                      |
|:------------------:| ---------------- | ------------- | ----------------------------------------------------------- |
| :heavy_check_mark: | Title Bar        |               |                                                             |
| :heavy_check_mark: |                  | end plug      | link from source-table                                      |
| :heavy_check_mark: |                  | icon          | source type                                                 |
| :heavy_check_mark: |                  | table name    |                                                             |
| :heavy_check_mark: |                  | start plug    | link to another transform table                             |
| :heavy_check_mark: | Column (List)    |               |                                                             |
| :heavy_check_mark: |                  | end plug      | link from another table                                     |
| :heavy_check_mark: |                  | icon          | data type                                                   |
| :heavy_check_mark: |                  | column name   |                                                             |
| :heavy_check_mark: |                  | start plug    | link to another table                                       |
| :heavy_check_mark: | Transform (List) |               | post transform                                              |
| :heavy_check_mark: |                  | separator     | gap between column list and output list                     |
| :heavy_check_mark: |                  | icon          | Transform Icon                                              |
| :heavy_check_mark: |                  | function name | Transform function name                                     |
| :heavy_check_mark: | Output (List)    |               |                                                             |
| :heavy_check_mark: |                  | separator     | gap between column list and output list                     |
| :heavy_check_mark: |                  | icon          | output type (db, txt, md, csv, unknown for newer)           |
| :heavy_check_mark: |                  | file name     | table name or file name                                     |
| :heavy_check_mark: |                  | start plug    | link to Step Output when this output defined as Step Output |

### TRANSFORM FUNCTION

| :heavy_check_mark: | section         |               | remark                  |
|:------------------:| --------------- | ------------- | ----------------------- |
| :heavy_check_mark: | Title Bar       |               |                         |
| :heavy_check_mark: |                 | icon          | transform function icon |
| :heavy_check_mark: |                 | function name |                         |
| :heavy_check_mark: |                 | start plug    | link to another table   |
| :heavy_check_mark: | Argument (List) |               |                         |
| :heavy_check_mark: |                 | end plug      | link from another table |
| :heavy_check_mark: |                 | icon          | data type               |
| :heavy_check_mark: |                 | argument name |                         |

### STEP OUTPUT

| :heavy_check_mark: | section   |            | remark                |
|:------------------:| --------- | ---------- | --------------------- |
| :heavy_check_mark: | Title bar |            |                       |
| :heavy_check_mark: |           | end plug   | link from owner table |
| :heavy_check_mark: |           | table name |                       |
| :heavy_check_mark: | Icon Area |            |                       |
| :heavy_check_mark: |           | icon       | output type           |
| :heavy_check_mark: |           | file name  |                       |

## PROPERTY SHEET

- How to build UI for different object
  
  - :x: create sheet by sheet (specific)
    - good: easy creation, control in UI part
    - bad: more specific page creation, can't apply to another page/dialog, change one activity for one data-type need to update to all specific pages
  - :heavy_check_mark: using prototype string in Enum constant
    - good: hard in start of creation and easiest later, control in Data Model part
    - bad: one dynamic page creation, change activities for any data-type in one page

- How to handle property for member object, list of object (map as list)
  
  - create sheet by sheet (specific)
  - using prototype string in another Enum constant by Enum data-type

- How many types of data
  
  1. primitive type (string,integer,decimal,date,datetime)
  2. data-source (id choosing from data-source list)
  3. data-table (id choosing from data-table list)
  4. transform-table (id choosing from transform-table list)
  5. column (id choosing from column list)]
     - case: column of selected table in another field
     - :heavy_multiplication_x: case: column of specified table id and type
     - case: column of active table
  6. column function (Enum constant)
  7. table function (Enum constant)
  8. local data file (upload file, local path(relative path))
  9. ftp data file (sftp connection id choosing from sftp connection list, root-path, local path for downloaded(relative path))
  10. htp data file (url, local path for downloaded)
  11. child (another Enum, child name as group title)
  12. child-list (list of another Enum, child name as group title)

- Who has the property sheet (selectable objects) [see: Properties Enum]
  
  1. DATA BASE (data source)
  2. SFTP CONNECTION (data source)
  3. FTP FILE (data file)
  4. LOCAL FILE (data file)
  5. DATA TABLE
  6. DATA COLUMN
  7. TRANSFORM TABLE
  8. TRANSFORM COLUMN
  9. COLUMN FX
  10. TABLE FX
  11. DATA OUTPUT
  12. :x: STEP OUTPUT FILE (same as data file)
  13. :x: STEP OUTPUT SOURCE (same as data file)

## PROTOTYPE STRING

```
<Member-Name>:<Property-Label>:<Property-Type>:param[:param]..
```

:heavy_check_mark: = completed

:x: = cancelled

| :heavy_check_mark: | name               | property<br /><sub>Fixed Text</sub> | data-type          | type params                         | default                   | remark                                                                                                                                                                                                                     |
|:------------------:| ------------------ | ----------------------------------- | ------------------ | ----------------------------------- | ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| :heavy_check_mark: | String             | STRING                              | String             |                                     |                           |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | max length                          | 1024                      |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | Regex Pattern (mask)                | no-specified              |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | password                            | false                     |                                                                                                                                                                                                                            |
| :heavy_check_mark: | String List        | STRINGLIST                          | String[]           |                                     |                           |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | separator character                 | \n                        |                                                                                                                                                                                                                            |
| :heavy_check_mark: | Boolean            | BOOL                                | boolean            | (none)                              | false                     |                                                                                                                                                                                                                            |
| :heavy_check_mark: | Integer            | INT                                 | int                |                                     |                           |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | max value                           | no-specified              |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | min value                           | no-specified              |                                                                                                                                                                                                                            |
|                    | Decimal            | DEC                                 | double             |                                     |                           |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | decimal positions (digit after dot) | 2                         |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | max value                           | no-specified              |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | min value                           | no-specified              |                                                                                                                                                                                                                            |
| :x:                | Date               |                                     |                    |                                     |                           |                                                                                                                                                                                                                            |
| :x:                | DateTime           |                                     |                    |                                     |                           |                                                                                                                                                                                                                            |
| :heavy_check_mark: | DBMS               | DBMS                                | DBMS               | (none)                              |                           | select from Enum                                                                                                                                                                                                           |
| :heavy_check_mark: | Data Source Type   | DATASOURCETYPE                      | DataSourceType     | (none)                              |                           | select from Enum                                                                                                                                                                                                           |
| :heavy_check_mark: | Data Source Id     | DATASOURCE                          | int                |                                     |                           | select from List                                                                                                                                                                                                           |
|                    |                    |                                     |                    | data source type                    | database or sftp or local | filter to show only the specified data-source-type<br/><br/>no-specified=then use value from field name as data source type instead                                                                                        |
|                    |                    |                                     |                    | field name                          | no-specified              | no-specified=show all without filter                                                                                                                                                                                       |
| :heavy_check_mark: | Selectable Id      | SOURCETABLE                         | int                |                                     |                           | select from List,<br />this list show only the tables appear before current table only                                                                                                                                     |
|                    | Data Table Id      | DATATABLE                           | int                |                                     |                           | select from List                                                                                                                                                                                                           |
|                    |                    |                                     |                    | all                                 | false                     | no-specified same as false<br />all = false hide active table<br />all = true show all tables                                                                                                                              |
|                    | Transform Table Id | TRANSFORMTABLE                      | int                |                                     |                           | select from List                                                                                                                                                                                                           |
|                    |                    |                                     |                    | all                                 | false                     | no-specified same as false<br />all = true show all transform table<br />all = false show transform tables with index before active transform table                                                                        |
| :heavy_check_mark: | Column Id          | COLUMN                              | String             |                                     |                           | select from List                                                                                                                                                                                                           |
|                    |                    |                                     |                    | field name                          | no-specified              | Property-Map-Name for Data-Table-SelectableID<br />no-specified = error                                                                                                                                                    |
|                    |                    |                                     |                    | id or name                          | id                        | use ID or Name as item-value                                                                                                                                                                                               |
| :mark              | Read Only          | ReadOnly                            | String             |                                     |                           | Read Only                                                                                                                                                                                                                  |
| :heavy_check_mark: | Column Function    | ColumnFunction                      | ColumnFunction     | (none)                              |                           | select from Enum                                                                                                                                                                                                           |
| :heavy_check_mark: | Table Function     | TableFunction                       | TableFunction      | (none)                              |                           | select from Enum                                                                                                                                                                                                           |
|                    | Upload File        | UPLOAD                              | String             |                                     |                           | uploaded file stored in sub directory /data/                                                                                                                                                                               |
|                    |                    |                                     |                    | file extension                      | *                         | comma separated value<br />value is file-extension without dot<br />specified extensions that allow to upload                                                                                                              |
| :heavy_check_mark: | File Type          | FILETYPE                            | DataFileType       |                                     |                           |                                                                                                                                                                                                                            |
|                    |                    |                                     |                    | in or out                           | requried                  | input or output                                                                                                                                                                                                            |
| :x:                |                    |                                     |                    | field name                          | no-specified              | @Deprecated<br />lets use the 'READ ONLY' and put some update code in the setFunction of the Upload field.<br />field of Upload File, the type is up to the uploaded file and read only<br />no-specified=select from Enum |
| :x:                | File Properties    | FILEPROP                            | Map<String,String> |                                     |                           | more property from DataFileType                                                                                                                                                                                            |
| :x:                |                    |                                     |                    | field name                          | no-specified              | field of FILE_TYPE                                                                                                                                                                                                         |
| :heavy_check_mark: | SFTP Connection    | FTP                                 | SFTP               | (none)                              |                           | select from List                                                                                                                                                                                                           |
|                    | FTP/SFTP File      | FTPFILE                             | String             |                                     |                           | downloaded file stored in sub directory /ftp/<SFTP-ID>/<SFTP-PATH>                                                                                                                                                         |
|                    |                    |                                     |                    | field name                          | no-specified              | field of SFTP Connection<br />no-specified = SFTP Connection that marked as default (Default SFTP Connection)                                                                                                              |
|                    | HTTP/HTTPS File    | HTTP                                | String             |                                     |                           | downloaded file stored in sub directory /http/<url>                                                                                                                                                                        |
|                    | Child              | CHILD                               |                    |                                     |                           | member-name of Child are relative to this member-name like this<br /><br />root-object [this-member-name] [child-member-name]                                                                                              |
|                    |                    |                                     |                    | enum name                           |                           | another enum constant                                                                                                                                                                                                      |
|                    |                    |                                     |                    | list                                | object                    | no-specfied = object<br />object = Child Object<br />list = list of Child Objects                                                                                                                                          |

----

## [Primefaces Built-In Themes](https://primefaces.github.io/primefaces/11_0_0/#/core/themes?id=built-in-themes)

PrimeFaces comes built-in with themes ready to use of out the box. They include:

- arya
- luna-amber (SELECTED for Dark Theme)
- luna-blue
- luna-green
- luna-pink
- nova-colored
- nova-dark
- nova-light
- saga (DEFAULT) (SELECTED for Light Theme)
- vela

## Primefaces Community Themes

> all-themes.jar

- afterdark
- afternoon
- afterwork
- aristo
- black-tie
- blitzer
- bluesky
- bootstrap
- casablanca
- cruze
- cupertino
- dark-hive
- delta
- dot-luv
- eggplant
- excite-bike
- flick
- glass-x
- home
- hot-sneaks
- humanity
- le-frog
- midnight
- mint-choc
- overcast
- pepper-grinder
- redmond
- rocket
- sam
- smoothness
- south-street
- start
- sunny
- swanky-purse
- themes-project
- trontastic
- ui-darkness
- ui-lightness
- vader

----

-- end of document --
