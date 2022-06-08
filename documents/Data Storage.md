# DATA STORAGE

----



## SYSTEM DATABASE

Data stored in system database.

| Data | Description |
| ---- | ----------- |
| User |             |
|      |             |



## SYSTEM NAS (file system)

Data stored in system NAS.

| Data                 | Description                                                  |
| -------------------- | ------------------------------------------------------------ |
| System Alert Message |                                                              |
| Project Files        | Serialized Data of Project (26 Files per Project) (14 tables when using DB) |
| Library Files        | uploaded library files use to build the Package File         |
| Package Files        | after build package<br />+ Zip Archive File for Robot Framework Package<br />+ Zip Archive File for Batch Process Package<br />+ Web Archive File for Batch Scheduler Package<br />+ Web Archive File for Web Service Package<br />+ Web Archive File for Web UI Package |



## GIT

| Data                    | Description                                                  |
| ----------------------- | ------------------------------------------------------------ |
| Versioned Project Files | Commit at the start of Export Package Process only the Project has some changes |
| Versioned Library Files | Commit at the start of Upload Library Process (library version update)<br /><small>DConvers and more library files.</small> |
| Versioned Package Files | Commit at the start of Export Package Process when the Project has some changes |

----

-- end of document --