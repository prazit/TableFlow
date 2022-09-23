package com.tflow.model.editor;

import com.tflow.model.editor.cmd.ExtractSystemEnvironment;
import com.tflow.model.editor.datasource.DataSourceType;

/**
 * Notice: IMPORTANT: must compatible to dataSourceName that used in DConvers.start().dataSourceMap.put(dataSourceName)
 */
public enum DataFileType {

    /*TODO: Future feature 'DataSourceType.KAFKAPRODUCER' is added also need to remove dataSourceType from this enum*/
    /*TODO: create real Extractor class for each INPUT types*/
    IN_MARKDOWN("Markdown File", "markdown.png", DataSourceType.LOCAL, Properties.INPUT_MARKDOWN, "input.md", "/(\\.|\\/)(md|markdown)$/", ExtractSystemEnvironment.class),
    IN_SQL("SQL File", "sql.png", /*DataSourceType.DATABASE*/ null, Properties.INPUT_SQL, "input.sql", "/(\\.|\\/)(sql)$/", ExtractSystemEnvironment.class),
    IN_DIR("Directory List", "dir.png", DataSourceType.LOCAL, Properties.INPUT_DIRECTORY, "/", null, ExtractSystemEnvironment.class),
    IN_ENVIRONMENT("System Environment", "system.png", DataSourceType.SYSTEM, Properties.INPUT_SYSTEM_ENVIRONMENT, "Environment", null, ExtractSystemEnvironment.class),

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

    private Class extractorClass;

    DataFileType(String name, String image, DataSourceType dataSourceType, Properties properties, String defaultFileName, String allowTypes, Class extractorClass) {
        this.name = name;
        this.image = image;
        this.dataSourceType = dataSourceType;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = allowTypes;
        this.extractorClass = extractorClass;
    }

    DataFileType(String name, String image, DataSourceType dataSourceType, Properties properties, String defaultFileName, String allowTypes) {
        this.name = name;
        this.image = image;
        this.dataSourceType = dataSourceType;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = allowTypes;
        this.extractorClass = null;
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

    public Class getExtractorClass() {
        return extractorClass;
    }
}
