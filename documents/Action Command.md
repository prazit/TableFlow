# ACTION COMMAND

Dev-Commands used in any User-Action.

----

|      :heavy_check_mark:       | Comamnd           | Param        | Param-DataType     | Description                                                  | Scope     |
| :---------------------------: | ----------------- | ------------ | ------------------ | ------------------------------------------------------------ | --------- |
| :negative_squared_cross_mark: | SetActiveProject  | project-file | String             | open project by file-name, load everything to memory.        | Workspace |
| :negative_squared_cross_mark: | SetActiveStep     | step-index   | integer            | open editor-page by step-index, index is sequenced number started at 0.<br /><br />result: <br />all existing objects are shown in the flowchart-page | Project   |
|      :heavy_check_mark:       | AddDataTable      | paramMap     | Map<String,Object> | compatible with DataFileType.paramMap but the Object part of the Map is user defined value.<br /><br />result:<br /><br />**add** Floor to DataTower<br />**add** DataSource to Floor as Room1<br />**add** DataFile to Floor as Room2<br />**load** DataTable Structure from Specified Source<br />**add** DataTable to Floor as Room3 | Step      |
|      :white_check_mark:       | AddTransformTable | paramMap     | Map<String,Object> |                                                              | Step      |
|                               | SetActiveRoom     |              |                    |                                                              |           |
|                               | UpdateDataTable   | propertyMap  | Map<String,Object> |                                                              |           |
|                               | AddTransformTable | dataTableId  | integer            | create TransformTable using DataTable as Source.             |           |
|                               |                   |              |                    |                                                              |           |

:negative_squared_cross_mark: = Cancelled

:white_check_mark: = On process

----

-- end of document --