# TODO LIST

----

:heavy_check_mark: = completed

:x: = cancelled

:white_check_mark: = in progress

|        Done        | Task                                                         | Remark                                                       |
| :----------------: | ------------------------------------------------------------ | ------------------------------------------------------------ |
| :heavy_check_mark: | Write to files: <br />need design of file structure          | see: File Structure.md                                       |
| :heavy_check_mark: | need storage selection,<br />                                | storages: (embedded or dbms) sqlite, berkeley, file or mongo, java-db<br /><br />**file system** is the best choice (fastest query time when have large amount of objects) |
|                    | need API tier for read/write object                          | back end: REST or learn more about MicroService(both client browser and server), Kafka (event queue) and friends |
|                    | need Write Queue System, <br />no wait time for Write, <br />guarantee no fail for Write <br /> | front end: <br />+ need low storage alert (show at the Project Selection step before open project)<br />+ need to lock all change when open Project with an storage alert |
|                    |                                                              | front end event: write when everything has changed<br />     |


----

-- end of document --