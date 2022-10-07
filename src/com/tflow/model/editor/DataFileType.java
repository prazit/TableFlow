package com.tflow.model.editor;

import com.tflow.model.editor.cmd.ExtractDirList;
import com.tflow.model.editor.cmd.ExtractMarkdown;
import com.tflow.model.editor.cmd.ExtractSystemEnvironment;

/**
 * Notice: IMPORTANT: must compatible to dataSourceName that used in DConvers.start().dataSourceMap.put(dataSourceName)
 */
public enum DataFileType {

    /*TODO: Future feature 'DataSourceType.KAFKAPRODUCER' is added also need to remove dataSourceType from this enum*/
    /*TODO: create real Extractor class for each INPUT types*/
    IN_SQL("SQL File", "sql.png", Properties.INPUT_SQL, "", "/(\\.|\\/)(sql)$/", ExtractSystemEnvironment.class),

    IN_MARKDOWN("Markdown File", "markdown.png", Properties.INPUT_MARKDOWN, "", "/(\\.|\\/)(md|markdown)$/", ExtractMarkdown.class),
    IN_DIR("Directory List", "dir.png", Properties.INPUT_DIRECTORY, "/", null, ExtractDirList.class),
    IN_ENVIRONMENT("System Environment", "system.png", Properties.INPUT_SYSTEM_ENVIRONMENT, "Environment", null, ExtractSystemEnvironment.class),

    OUT_MD("Markdown File", "markdown.png", Properties.OUTPUT_MARKDOWN, "output.md"),
    OUT_CSV("CSV File", "csv.png", Properties.OUTPUT_CSV, "output.csv"),
    OUT_TXT("Fixed Length File", "txt.png", Properties.OUTPUT_TXT, "output.txt"),
    OUT_SQL("SQL File", "sql.png", Properties.OUTPUT_SQL, "output.sql"),
    OUT_INS("DB Insert", "sql.png", Properties.OUTPUT_DBINSERT, ""),
    OUT_UPD("DB Update", "sql.png", Properties.OUTPUT_DBUPDATE, ""),
    ;

    private String name;
    private String image;
    private Properties properties;

    /**
     * for p:fileupload.allowTypes
     */
    private String allowTypes;

    private String defaultFileName;

    private Class extractorClass;

    DataFileType(String name, String image, Properties properties, String defaultFileName, String allowTypes, Class extractorClass) {
        this.name = name;
        this.image = image;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = allowTypes;
        this.extractorClass = extractorClass;
    }

    DataFileType(String name, String image, Properties properties, String defaultFileName) {
        this.name = name;
        this.image = image;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
        this.allowTypes = null;
        this.extractorClass = null;
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

    public String getAllowTypes() {
        return allowTypes;
    }

    public Class getExtractorClass() {
        return extractorClass;
    }
}
