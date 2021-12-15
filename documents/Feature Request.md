# Features Request

Table Flow Concept 2021

----

Command based design for Action History with Undo/Redo and git operations.

----

## ACTION REQUESTED

| Action                                                     | Remark / Description                                         |
| ---------------------------------------------------------- | ------------------------------------------------------------ |
| New Project (single step with blank diagram)               |                                                              |
|                                                            |                                                              |
| Add - Database Connection                                  |                                                              |
| Add - FTP/SFTP Connection                                  |                                                              |
| Add - HTTP/HTTPS Connection                                |                                                              |
|                                                            |                                                              |
| -- source table --                                         |                                                              |
| Add - Table from Database (SQL)                            |                                                              |
| Add - Table from Data File ... (CSV, TXT, Markdown)        |                                                              |
| Modify - Table Properties                                  | UI: name, index, datasource, query, id                       |
| Remove - Table                                             |                                                              |
|                                                            |                                                              |
| -- target table --                                         |                                                              |
| Add - Table from Existing Table (Transfer Single Table)    |                                                              |
| Add - Table from Existing Tables (Merge Multiple Table)    |                                                              |
| Modify - Table Properties                                  | UI: name, index, source-table, query, id                     |
| Remove - Table                                             |                                                              |
| Add - Column (Blank)                                       |                                                              |
| Add - Lookup Column Link                                   | ACT: Drag Column Link to Column in another table             |
| Modify - Column                                            | UI: First column property is the column type and other properties are depending on the selected column type.<br />In case of Transferred Column / Lookup Column need to Remove Link before change the Column Type. |
| Remove - Transferred Column Link                           | ACT: Ctrl + Right click on the Column Link to Remove Link    |
| Remove - Lookup Column Link                                | ACT: Ctrl + Right click on the Column Link to Remove Link    |
|                                                            |                                                              |
| Add - Output to File ... (CSV, TXT, Markdown)              |                                                              |
| Add - Output to Database (INSERT (replace/append), UPDATE) |                                                              |
|                                                            |                                                              |
| Add Step                                                   |                                                              |
| Add Step from ... (copy existing step)                     |                                                              |
| New Project from ... (copy existing project)               |                                                              |



## IMPROVEMENT

| Module                                          | Improvement                  |
| ----------------------------------------------- | ---------------------------- |
| Editor > Sidebars (step list and property list) | need to hide or show by user |



----

-- end of document --