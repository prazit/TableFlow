package com.tflow.model.editor;

import com.tflow.model.editor.cmd.*;
import com.tflow.model.editor.datasource.DataSourceType;

/**
 * Notice: IMPORTANT: must compatible to dataSourceName that used in DConvers.start().dataSourceMap.put(dataSourceName)
 */
public enum DataFileType {

    /*TODO: Future feature 'DataSourceType.KAFKAPRODUCER' is added also need to remove dataSourceType from this enum*/
    IN_MARKDOWN("Markdown File", "markdown.png", Properties.INPUT_MARKDOWN, "", "/(\\.|\\/)(md|markdown)$/", DataSourceType.FIXED, ExtractMarkdown.class),
    IN_SQL("SQL Insert File", "sql.png", Properties.INPUT_SQLI, "", "/(\\.|\\/)(sql)$/", DataSourceType.FIXED, ExtractSQLInsert.class),
    IN_SQLDB("SQL Select File", "sql.png", Properties.INPUT_SQL, "", "/(\\.|\\/)(sql)$/", DataSourceType.DATABASE, ExtractSQLSelect.class),
    IN_DIR("Directory List", "dir.png", Properties.INPUT_DIRECTORY, "/", null, DataSourceType.DIR, ExtractDirList.class),
    IN_ENVIRONMENT("System Environment", "system.png", Properties.INPUT_SYSTEM_ENVIRONMENT, "Environment", null, DataSourceType.SYSTEM, ExtractSystemEnvironment.class),

    OUT_MD("Markdown File", "markdown.png", Properties.OUTPUT_MARKDOWN, "$[CAL:NAME(CURRENT)].md"),
    OUT_CSV("CSV File", "csv.png", Properties.OUTPUT_CSV, "$[CAL:NAME(CURRENT)].csv"),
    OUT_TXT("Fixed Length File", "txt.png", Properties.OUTPUT_TXT, "$[CAL:NAME(CURRENT)].txt"),
    OUT_SQL("SQL File", "sql.png", Properties.OUTPUT_SQL, "$[CAL:NAME(CURRENT)].sql"),
    OUT_INS("DB Insert", "sql.png", Properties.OUTPUT_DBINSERT, ""),
    OUT_UPD("DB Update", "sql.png", Properties.OUTPUT_DBUPDATE, ""),
    ;

    private String name;
    private String image;
    private Properties properties;
    private DataSourceType dataSourceType;

    /**
     * for p:fileupload.allowTypes
     */
    private String allowTypes;

    private String defaultFileName;

    private Class extractorClass;

    DataFileType(String name, String image, Properties properties, String defaultFileName, String allowTypes, DataSourceType dataSourceType, Class extractorClass) {
        this.name = name;
        this.image = image;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = allowTypes;
        this.extractorClass = extractorClass;
        this.dataSourceType = dataSourceType;
    }

    DataFileType(String name, String image, Properties properties, String defaultFileName) {
        this.name = name;
        this.image = image;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = null;
        this.extractorClass = null;
        this.dataSourceType = null;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
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

    public boolean isRequireDatabase() {
        return DataFileType.IN_SQLDB == this;
    }

    public String getAllowTypes() {
        return allowTypes;
    }

    public Class getExtractorClass() {
        return extractorClass;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }
}
