# FILE STRUCTURE

Project data based.

----

> need smallest file
>
> conceptual: a little update need a little write.

| Level 0       | 1             | 2                  | 3                      | Data                                                         | all  | unique<br />obj | db   |
| ------------- | ------------- | ------------------ | ---------------------- | ------------------------------------------------------------ | :--: | :-------------: | ---- |
| group-id-list |               |                    |                        | Group List:<br />+ list of group-id (+display)               |      |                 |      |
| group-id      |               |                    |                        | Project List:<br />+ list of project-id (+display) within a group |      |                 |      |
| project-id    |               |                    |                        | Project Settings:<br />+ settings                            |  1   |        1        | 1    |
|               | db-id-list    |                    |                        | Database List:<br />+ list of db-id                          |  2   |        2        |      |
|               | sftp-id-list  |                    |                        | SFTP List:<br />+ list of sftp-id                            |  3   |                 |      |
|               | local-id-list |                    |                        | Root Directory List:<br />+ list of local-id                 |  4   |                 |      |
|               | step-id-list  |                    |                        | Step List:<br />+ list of step-id                            |  5   |                 |      |
|               | db-id         |                    |                        | Database Connection:<br />+ connection detail                |  6   |        3        | 2    |
|               | sftp-id       |                    |                        | SFTP Connection:<br />+ connection detail                    |  7   |        4        | 3    |
|               | local-id      |                    |                        | Root Directory:<br />+ directory detail                      |  8   |        5        | 4    |
|               | step-id       |                    |                        | Step: <br />+ step detail                                    |  9   |        6        | 5    |
|               |               | data-table-id-list |                        | Data Table List:<br />+ list of data-table-id                |  10  |                 |      |
|               |               | tower-id           |                        | Tower: <br />+ list of floor-id                              |  11  |                 |      |
|               |               | floor-id           |                        | Floor:<br />+ list of room-id (object-id)                    |  12  |                 | 6    |
|               |               | line-id-list       |                        | Line List:<br />+ list of line-id                            |  13  |                 |      |
|               |               |                    | line-id                | Line:<br />+ line detail                                     |  14  |        7        | 7    |
|               |               | data-file-id       |                        | Data File:<br />+ file detail<br />+ data-source-id (db-id \| sftp-id \| local-id) |  15  |        8        | 8    |
|               |               | data-table-id      |                        | Data Table:<br />+ table detail<br />+ data-file-id          |  16  |        9        | 9    |
|               |               |                    | column-id-list         | Data Column List:<br />+ list of column-id                   |  17  |                 |      |
|               |               |                    | output-file-id-list    | Output File List:<br />+ list of output-file-id              |  18  |                 |      |
|               |               |                    | column-id              | Data Column:<br />+ column detail                            |  19  |       10        | 10   |
|               |               | transform-table-id |                        | Transform Table:<br />+ table detail<br />+ data-table-id (source) |  20  |       11        | 11   |
|               |               |                    | column-id-list         | Column List:<br />+ list of transform-column-id              |  21  |                 |      |
|               |               |                    | transformation-id-list | Transformation List:<br />+ list of transformation-id        |  22  |                 |      |
|               |               |                    | output-file-id-list    | Output File List:<br />+ list of output-file-id              |  23  |                 |      |
|               |               |                    | transform-column-id    | Transform Column:<br />+ column detail<br />(different from Data Column) |  24  |       12        | 12   |
|               |               |                    | transformation-id      | Transformation:<br />+ transformation detail                 |  25  |       13        | 13   |
|               |               |                    | output-file-id         | Output File:<br />+ output detail                            |  26  |       14        | 14   |

----

-- end of document --