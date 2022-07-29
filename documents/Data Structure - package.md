# FILE STRUCTURE

##### Package data & Binary files

---

## Package Type and Binary Files

| Package Type                        | file path                                                   | files                                      | file type                                                           | download as |
|:-----------------------------------:| ----------------------------------------------------------- | ------------------------------------------ | ------------------------------------------------------------------- | ----------- |
| 1. Batch                            | sql/                                                        | sql data files                             | sql                                                                 | zip         |
|                                     | md/                                                         | md data files                              | md                                                                  |             |
|                                     | txt/                                                        | txt data files                             | txt                                                                 |             |
|                                     | config/                                                     | dconvers config files                      | property                                                            |             |
|                                     | lib/                                                        | dconvers lib files                         | jar                                                                 |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             |             |
| 2. Kafka Service<br/>3. Web service | lib/                                                        | compiled java class in a jar file per step | temp: java, class, META-INF/MANIFEST.MF, logback.xml<br/>built: jar | zip         |
|                                     | sql/                                                        | sql data files                             | all supported data files                                            |             |
|                                     | md/                                                         | md data files                              |                                                                     |             |
|                                     | txt/                                                        | txt data files                             |                                                                     |             |
|                                     | config/                                                     | dconvers config files                      | property                                                            |             |
|                                     | lib/                                                        | dconvers lib files                         | jar                                                                 |             |
|                                     | batch/                                                      | batch/shell files                          | bat, sh                                                             |             |
| 4. Web UI Input<br/>5. Web Service  | WEB-INF/classes/                                            | compiled java class files<br/>             | temp: java<br/>built: class                                         | war         |
|                                     | resources/js/<br/>resources/images/<br/>resources/css/<br/> | web ui resource files                      | js<br/>png<br/>css                                                  |             |
|                                     | cutom-ui-path/                                              | web ui xhtml files                         | xhtml                                                               |             |
|                                     | WEB-INF/                                                    | web ui config files                        | web.xml, logback.xml                                                |             |
|                                     | WEB-INF/lib/                                                | web ui lib files                           | jar                                                                 |             |
|                                     | WEB-INF/sql/                                                | sql data files                             | sql                                                                 |             |
|                                     | WEB-INF/md/                                                 | md data files                              | md                                                                  |             |
|                                     | WEB-INF/txt/                                                | txt data files                             | txt                                                                 |             |
|                                     | WEB-INF/config/                                             | dconvers config files                      | property                                                            |             |
|                                     | WEB-INF/lib/                                                | dconvers lib files                         | jar                                                                 |             |

## Package List

> List<PackageLabel> of Package-ID

| field     | desc                | type   |
| --------- | ------------------- | ------ |
| packageId | formatted date time | String |
| name      | user custom label   | String |

#### Package Data

| field     | desc                      | type        |
| --------- | ------------------------- | ----------- |
| packageId | formatted date time       | String      |
| projectId | owner projectID           | int         |
| name      | user custom label         | String      |
| buildDate | bulid date and time       | Date (util) |
| builtDate | built date and time       | Date (util) |
| complete  | 0 - 100 percent completed | int         |

#### Package Data.FileList

> List<PackageFile>

| field   | desc                          | type        |
| ------- | ----------------------------- | ----------- |
| updated | differenced from last package | boolean     |
| path    | file path                     | String      |
| name    | file name                     | String      |
| type    | file type                     | Enum String |
| time    | built date time               | Date (util) |

----

-- end of document --
