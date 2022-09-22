package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;

/**
 * Notice: IMPORTANT: must compatible to dataSourceName that used in DConvers.start().dataSourceMap.put(dataSourceName)
 */
public enum DataFileType {

    /*TODO: when future feature 'DataSourceType.KAFKAPRODUCER' is added also need to remove dataSourceType from this enum*/
    IN_MARKDOWN("Markdown File", "markdown.png", DataSourceType.LOCAL, Properties.INPUT_MARKDOWN, "input.md", "/(\\.|\\/)(md|markdown)$/"),
    IN_SQL("SQL File", "sql.png", /*DataSourceType.DATABASE*/ null, Properties.INPUT_SQL, "input.sql", "/(\\.|\\/)(sql)$/"),
    IN_DIR("Directory List", "dir.png", DataSourceType.LOCAL, Properties.INPUT_DIRECTORY, "/", null),
    IN_ENVIRONMENT("System Environment", "system.png", DataSourceType.SYSTEM, Properties.INPUT_SYSTEM_ENVIRONMENT, "Environment", null),

    /*-- TODO: Future Feature: output to Database
    OUT_DBINSERT("Insert to Database", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBINSERT, "no-output-file", ""),
    OUT_DBUPDATE("Update to Database", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBUPDATE, "no-output-file", ""),*/

    OUT_SQL("SQL File", "sql.png", DataSourceType.LOCAL, Properties.OUTPUT_SQL, "output.sql", null),
    OUT_MD("Markdown File", "markdown.png", DataSourceType.LOCAL, Properties.OUTPUT_MARKDOWN, "output.md", null),
    OUT_CSV("CSV File", "csv.png", DataSourceType.LOCAL, Properties.OUTPUT_CSV, "output.csv", null),
    OUT_TXT("Fixed Length File", "txt.png", DataSourceType.LOCAL, Properties.OUTPUT_TXT, "output.txt", null),
    ;

    private String name;
    private String image;
    private Properties properties;

    /**
     * for p:fileupload.allowTypes
     */
    private String allowTypes;

    @Deprecated
    private DataSourceType dataSourceType;

    private String defaultFileName;

    DataFileType(String name, String image, DataSourceType dataSourceType, Properties properties, String defaultFileName, String allowTypes) {
        this.name = name;
        this.image = image;
        this.dataSourceType = dataSourceType;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = allowTypes;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getDefaultName() {
        return defaultFileName;
    }

    public boolean isInput() {
        return name().substring(0, 1).equals("I");
    }

    public boolean isOutput() {
        return name().substring(0, 1).equals("O");
    }

    public String getAllowTypes() {
        return allowTypes;
    }
}
