# FILE STRUCTURE

##### Package data & Binary files

---

## File Types

> TFlow = flowchart editor page, library page
> TWcmd = data writer
> TRcmd = data reader
> TBcmd = package builder
> TDcmd = package downloader & deployer

| type      | desc                        | Write                                      | Read           | identifier                              |
| --------- | --------------------------- | ------------------------------------------ | -------------- | --------------------------------------- |
| uploaded  | uploaded files from editor  | TFlow >> TWcmd : ProjectFileType.UPLOADED  | TDcmd >> TRcmd | projectID<br/>~~stepID~~<br/>fileID     |
| generated | saved using dconvers        | TBcmd >> TWcmd : ProjectFileType.GENERATED | TDcmd >> TRcmd | projectID<br/>~~stepID~~<br/>fileID     |
| versioned | uploaded files from library | TFlow >> TWcmd : ProjectFileType.VERSIONED | TDcmd >> TRcmd | ~~projectID~~<br/>~~stepID~~<br/>fileID |

## Package Type and Binary Files

| Package Type                        | file path                                                   | files                                      | file extension                                                      | file type                                  | download as |
|:-----------------------------------:| ----------------------------------------------------------- | ------------------------------------------ | ------------------------------------------------------------------- | ------------------------------------------ | ----------- |
| 1. Batch                            | sql/                                                        | sql data files                             | sql                                                                 | uploaded                                   | zip         |
|                                     | md/                                                         | md data files                              | md                                                                  | uploaded                                   |             |
|                                     | txt/                                                        | txt data files                             | txt                                                                 | uploaded                                   |             |
|                                     | config/                                                     | dconvers config files                      | property                                                            | generated                                  |             |
|                                     | lib/                                                        | dconvers lib files                         | jar                                                                 | versioned : library-file-id                |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             | generated triggers                         |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             | generated<br/>schedule adder               |             |
|                                     | (none)                                                      | scheduler                                  | (none)                                                              | starter                                    |             |
| 2. Kafka Service<br/>3. Web service | lib/                                                        | compiled java class in a jar file per step | temp: java, class, META-INF/MANIFEST.MF, logback.xml<br/>built: jar | generated<br/>compiled<br/>zipped triggers | zip         |
|                                     | sql/                                                        | sql data files                             | sql                                                                 | uploaded                                   |             |
|                                     | md/                                                         | md data files                              | md                                                                  | uploaded                                   |             |
|                                     | txt/                                                        | txt data files                             | txt                                                                 | uploaded                                   |             |
|                                     | config/                                                     | dconvers config files                      | property                                                            | generated                                  |             |
|                                     | lib/                                                        | dconvers lib files                         | jar                                                                 | versioned                                  |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             | generated starter                          |             |
| 4. Web UI Input<br/>5. Web Service  | WEB-INF/classes/                                            | compiled java class files<br/>             | temp: java<br/>built: class                                         |                                            | war<br/>zip |
|                                     | resources/js/<br/>resources/images/<br/>resources/css/<br/> | web ui resource files                      | js<br/>png<br/>css                                                  |                                            |             |
|                                     | cutom-ui-path/                                              | web ui xhtml files                         | xhtml                                                               | triggers                                   |             |
|                                     | WEB-INF/                                                    | web ui config files                        | web.xml, logback.xml                                                |                                            |             |
|                                     | WEB-INF/lib/                                                | web ui lib files                           | jar                                                                 |                                            |             |
|                                     | WEB-INF/sql/                                                | sql data files                             | sql                                                                 |                                            |             |
|                                     | WEB-INF/md/                                                 | md data files                              | md                                                                  |                                            |             |
|                                     | WEB-INF/txt/                                                | txt data files                             | txt                                                                 |                                            |             |
|                                     | WEB-INF/config/                                             | dconvers config files                      | property                                                            |                                            |             |
|                                     | WEB-INF/lib/                                                | dconvers lib files                         | jar                                                                 |                                            |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             | genreated<br/>web deployer                 |             |
|                                     | (none)                                                      | web server                                 | (none)                                                              | starter                                    |             |

## Package List

> List<PackageItemData> of Package-ID

| field     | desc                | type   |
| --------- | ------------------- | ------ |
| packageId | formatted date time | String |
| name      | user custom label   | String |

#### Package Data

| field     | desc                      | type                  |
| --------- | ------------------------- | --------------------- |
| packageId | formatted date time       | String                |
| projectId | owner projectID           | int                   |
| name      | user custom label         | String                |
| buildDate | bulid date and time       | Date (util)           |
| builtDate | built date and time       | Date (util)           |
| complete  | 0 - 100 percent completed | int                   |
| fileList  | list of file              | List<PackageFileData> |

### PackageFileData

| field     | desc                                                   | type                                                 |
| --------- | ------------------------------------------------------ | ---------------------------------------------------- |
| id        | package file id                                        | int                                                  |
| name      | file name                                              | String                                               |
| type      | file type                                              | uploaded, versioned, generated                       |
| fileId    | uploaded file id, versioned file id, generated file id | depends on file type                                 |
| ext       | file ext                                               | Enum String (short form used to access to mime type) |
| buildDate | same value on package                                  | Date (util)                                          |
| buildPath | file path used in archive/download process             | String                                               |
| updated   | compare result/differenced from last package           | boolean                                              |

### BinaryFileData

| for VERSIONED and UPLOADED

| name | desc      | type   |
| ---- | --------- | ------ |
| id   | file id   | int    |
| name | file name | String |
| ext  | file ext  |        |

----

-- end of document --
