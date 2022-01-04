# TODO LIST

----

:heavy_check_mark: = completed

:x: = cancelled

:white_check_mark: = in progress

|        Done        | Task                                                         | Remark                                                       |
| :----------------: | ------------------------------------------------------------ | ------------------------------------------------------------ |
|                    | Show all objects in **Mockup Data Mode** using Commands      |                                                              |
| :heavy_check_mark: | - Show Step List and Open Step Flowchart                     | (no undo)                                                    |
| :heavy_check_mark: | - Show DataSource                                            | Command: AddDataTable                                        |
| :heavy_check_mark: | - Show DataFile                                              | Command: AddDataTable                                        |
| :heavy_check_mark: | - Show DataTable                                             | Command: AddDataTable                                        |
| :heavy_check_mark: | - Show Lines                                                 |                                                              |
| :heavy_check_mark: | - Show Transformation Table                                  | Command: AddTransformTable <br />**(triggered from endPlug link)** |
|                    | - Show New Column                                            | Command: AddColumn (transformTable)                          |
|                    | - Show Column Fx Table                                       | Command: AddFxColumn<br />**(triggered from startPlug link)** |
| :heavy_check_mark: | - Selectable Room                                            | Command: SelectRoom<br /><br />why position of data-table is 1,1 like this<br />![image-20211228153251022](C:\Users\prazi\AppData\Roaming\Typora\typora-user-images\image-20211228153251022.png)<br />fixed: change to use .offset instead of .position |
| :white_check_mark: | - Show Property Sheet of the Objects                         | layout one sheet using prototype string<br />**UI: Complete all PropertyTypes.**<br />**UI: Update property one by one when lost the focus.** |
| :heavy_check_mark: | DataStructure: change step output to outputList and the DataOutput need a link back to owner table. |                                                              |
| :heavy_check_mark: | DataStructure: add Tower Floor Room to the Step              |                                                              |
| :heavy_check_mark: | Data Structure: all objects                                  |                                                              |
| :heavy_check_mark: | UI: Re-design Column Function to show one line information to support all columns with the function. |                                                              |
| :heavy_check_mark: | DataStructure: Change Workspace to SessionScope and remove Workspace from Application. | Accesses of the Theme need to change from Application to Workspace. |
| :heavy_check_mark: | UI: Zoom input need to show number in percent.               | 1 = 100%                                                     |



----

-- end of document --