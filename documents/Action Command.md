# ACTION COMMAND

Dev-Commands used in any User-Action.

----

|      :heavy_check_mark:       | Comamnd           | Param | Param-DataType | Description                                                  | Scope     |
| :---------------------------: | ----------------- | ----- | -------------- | ------------------------------------------------------------ | --------- |
| :negative_squared_cross_mark: | SetActiveProject  |       |                | open project by file-name, load everything to memory.        | Workspace |
| :negative_squared_cross_mark: | SetActiveStep     |       |                | open editor-page by step-index, index is sequenced number started at 0.<br /><br />result: <br />all existing objects are shown in the flowchart-page | Project   |
|      :heavy_check_mark:       | AddDataSource     |       |                |                                                              | Project   |
|      :heavy_check_mark:       | AddDataFile       |       |                |                                                              | Step      |
|      :heavy_check_mark:       | AddDataTable      |       |                |                                                              | Step      |
|      :white_check_mark:       | AddColumn         |       |                | ActiveObject is DataTable only                               |           |
|      :white_check_mark:       | AddOutput         |       |                | ActiveObject is DataTable only                               |           |
|                               | AddTransformTable |       |                | ActiveObject is DataTable only<br />create TransformTable using DataTable as Source. | Step      |
|                               | AddColumnFx       |       |                | ActiveObject is TransformColumn only                         |           |
|                               | AddTableFx        |       |                | ActiveObject is TransformTable only                          |           |
|                               |                   |       |                |                                                              |           |
|                               |                   |       |                |                                                              |           |
|                               |                   |       |                |                                                              |           |
|                               |                   |       |                |                                                              |           |

:negative_squared_cross_mark: = Cancelled

:white_check_mark: = On process

----

-- end of document --