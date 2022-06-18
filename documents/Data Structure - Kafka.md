# DATA STRUCTURE

Kafka

----

## 

## NOTES

| name              | notes                                                                                                                                                                                                                     |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| kafka server down | consumer: work fine without problem<br/>producer: need to know Status of server to re-send all messages                                                                                                                   |
| topic             | = trigger-id, แยก topic ตาม Main Operation Type                                                                                                                                                                           |
| partition         | partition key = ระบุ Message Queue ด้วยหมายเลข<br/>จำนวน Partition กำหนดขณะที่สร้าง Topic                                                                                                                                 |
|                   | replication = กำหนด Partition Factor ขณะที่สร้าง Topic                                                                                                                                                                    |
| record-key        | ใช้เป็น Opearation Subtype ID                                                                                                                                                                                             |
| record-value      | JSON Formatted is work fine                                                                                                                                                                                               |
| consumer-group-id | group id เดียวกันจะมีเพียง 1 consumer ที่ได้รับ message สำหรับทำ load balancing<br/><br/>เฉพาะกรณี Writer มีการประมวลผลข้อมูลนาน ก่อนจะบันทึกผลลัพธ์<br/>และกรณี Reader มีการประมวลผลข้อมูลนาน ก่อนจะตอบสนองผลลัพธ์กลับไป |

## 

## PRODUCER METRICS

| cases                                      | statistic values                                     | remark                                                 |
| ------------------------------------------ | ---------------------------------------------------- | ------------------------------------------------------ |
| create producer when **server still down** | creationCount = 0<br/>closeCount = 0                 |                                                        |
| create producer when **server is up**      | creationCount = 1, 2, ...<br/>closeCount = 0         |                                                        |
| check producer after **server is down**    | creationCount = 2, 3, ...<br/>closeCount = 1, 2, ... |                                                        |
| check producer when **server is back**     | creationCount = 2, 3, ...<br/>closeCount = 1, 2, ... | :car: need to find difference between 'server is down' |

## 

## Group List Write Command

> **Kafka-Topic:** UpdateGroupList, UpdateProjectList
> 
> 
> **Note:** write process will read from data-file to data-record and update data-record by message-record before write to data-file.
> 
> **Required:** Client-ID

| record-key                            | data structure | remark |
| ------------------------------------- | -------------- | ------ |
| :car: need opeartion for group-list   |                |        |
| :car: need opeartion for project-list |                |        |

## 

## Project Write Command

> **Kafka-Topic:** <u>UpdateProject</u>
> 
> 
> **Note:** write process will read from data-file to data-record and update data-record by message-record before write to data-file.
> 
> **Kafka-Record-Detail:** already defined in [Data Structure - project.md](C:\Users\prazi\Documents\GitHub\TFlow\documents\Data Structure - project.md)

```json
// ### Message Record Value Structure is Concatenation of Serialized String
// 
// separator = TFLOW-ADDITIONAL-DATA
<serialized-data><separator><serialized-additional>

// ### Additional Data
{ 
    /* Parent Field Group: all fields are optional */
    projectId: "String",
    stepId: "String",
    dataTableId: "String",
    transformTableId: "String",

    /* Transaction Field Group: all fields are required */
    modifiedClientId: ,
    modifiedUserId: ,

    /* Generated Field Group: generate by service */
    createdClientId: ,    // copy from modifiedClientId when create
    createdUserId: ,
    createdDate: ,
    modifiedDate: ,       // server date
}
```

| data-key                       | shorten and use as record key | required field                                      |
| ------------------------------ | ----------------------------- | --------------------------------------------------- |
| project                        | project                       | + project-id                                        |
| db-list                        | db-list                       | + project-id                                        |
| sftp-list                      | sftp-list                     | + project-id                                        |
| local-list                     | local-list                    | + project-id                                        |
| step-list                      | step-list                     | + project-id                                        |
| db                             | db                            | + project-id                                        |
| sftp                           | sftp                          | + project-id                                        |
| local                          | local                         | + project-id                                        |
| step                           | step                          | + project-id                                        |
| data-table-list                | data-table-list               | + project-id<br/>+ step-id                          |
| tower                          | tower                         | + project-id<br/>+ step-id                          |
| floor                          | floor                         | + project-id<br/>+ step-id                          |
| line-list                      | line-list                     | + project-id<br/>+ step-id                          |
| line                           | line                          | + project-id<br/>+ step-id                          |
| data-file                      | data-file                     | + project-id<br/>+ step-id                          |
| data-table                     | data-table                    | + project-id<br/>+ step-id<br/>+ data-table-id      |
| data-table-column-list         | data-column-list              | + project-id<br/>+ step-id<br/>+ data-table-id      |
| data-table-output-list         | data-output-list              | + project-id<br/>+ step-id<br/>+ data-table-id      |
| data-table-column              | data-column                   | + project-id<br/>+ step-id<br/>+ data-table-id      |
| transform-table                | transform-table               | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-column-list    | transform-column-list         | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-trasnform-list | transformation-list           | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-output-list    | transform-output-list         | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-column         | transform-column              | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-transformation | transformation                | + project-id<br/>+ step-id<br/>+ transform-table-id |
| transform-table-output         | transform-output              | + project-id<br/>+ step-id<br/>+ transform-table-id |

----

-- end of document --
