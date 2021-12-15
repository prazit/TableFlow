# UIs

Table Flow v.0.0.0

----

## APPLICATION FRAME

| :heavy_check_mark: | section     |                       | remark                                  |
| :----------------: | ----------- | --------------------- | --------------------------------------- |
| :heavy_check_mark: | top section |                       | cover logo and menu                     |
| :heavy_check_mark: |             | company logo and name | left side of the top section            |
| :heavy_check_mark: |             | menu bar              | right side of the top section, dropdown |
| :heavy_check_mark: | screen area |                       | workspace for any screen content        |



## TFLOW EDITOR SCREEN

| :heavy_check_mark: | section       |                      | remark                                                       |
| :----------------: | ------------- | -------------------- | ------------------------------------------------------------ |
| :heavy_check_mark: | Workspace     |                      | Cover all sections of editor (screen area)                   |
| :heavy_check_mark: | Step List     |                      | **left side panel**                                          |
| :heavy_check_mark: | TFlow Area    |                      |                                                              |
| :heavy_check_mark: |               | Data Table Area      | Data Source<br />Data Table<br />Data Table Output           |
| :heavy_check_mark: |               | Transform Table Area | Transform Table<br />Transform Output                        |
| :heavy_check_mark: |               | Step Output Area     | Table Output                                                 |
| :heavy_check_mark: | Property List |                      | **right side panel**<br /><br />Project Properties<br />Step Properties<br />Database Connection Properties<br />SFTP Connection Properties<br />Data Table Properties<br />Transform Table Properties<br />Output Properties |



### DATA SOURCE

| :heavy_check_mark: |           | section          | remark                                               |
| :----------------: | --------- | ---------------- | ---------------------------------------------------- |
| :heavy_check_mark: | Title bar |                  |                                                      |
| :heavy_check_mark: |           | data source name |                                                      |
| :heavy_check_mark: | Icon Area |                  |                                                      |
| :heavy_check_mark: |           | icon             | data source type (database, local file, sftp, https) |
| :heavy_check_mark: |           | type name        |                                                      |
| :heavy_check_mark: |           | start plug       | ling to data-table                                   |



### DATA TABLE 

| :heavy_check_mark: | Section       |             | remark                                                      |
| :----------------: | ------------- | ----------- | ----------------------------------------------------------- |
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
| :----------------: | ---------------- | ------------- | ----------------------------------------------------------- |
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
| :----------------: | --------------- | ------------- | ----------------------- |
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
| :----------------: | --------- | ---------- | --------------------- |
| :heavy_check_mark: | Title bar |            |                       |
| :heavy_check_mark: |           | end plug   | link from owner table |
| :heavy_check_mark: |           | table name |                       |
| :heavy_check_mark: | Icon Area |            |                       |
| :heavy_check_mark: |           | icon       | output type           |
| :heavy_check_mark: |           | file name  |                       |

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