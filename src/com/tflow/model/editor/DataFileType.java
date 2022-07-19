package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;

public enum DataFileType {

    IN_SQL("SQL", "sql.png", DataSourceType.DATABASE, Properties.INPUT_SQL, "input.sql"),
    IN_MD("Markdown", "markdown.png", DataSourceType.LOCAL, Properties.INPUT_MARKDOWN, "input.md"),
    IN_ENV("System Environment", "system.png", DataSourceType.SYSTEM, Properties.INPUT_ENVIRONMENT, "no-input-file"),
    IN_DIR("Directory List", "dir.png", DataSourceType.LOCAL, Properties.INPUT_DIRECTORY, "/"),

    OUT_DBINSERT("Insert", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBINSERT, "no-output-file"),
    OUT_DBUPDATE("Update", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBUPDATE, "no-output-file"),
    OUT_SQL("SQL", "sql.png", DataSourceType.LOCAL, Properties.OUTPUT_SQL, "output.sql"),
    OUT_MD("Markdown", "markdown.png", DataSourceType.LOCAL, Properties.OUTPUT_MARKDOWN, "output.md"),
    OUT_CSV("CSV", "csv.png", DataSourceType.LOCAL, Properties.OUTPUT_CSV, "output.csv"),
    OUT_TXT("TXT", "txt.png", DataSourceType.LOCAL, Properties.OUTPUT_TXT, "output.txt"),
    ;

    private String name;
    private String image;
    private Properties properties;
    private DataSourceType dataSourceType;

    private String defaultFileName;

    DataFileType(String name, String image, DataSourceType dataSourceType, Properties properties, String defaultFileName) {
        this.name = name;
        this.image = image;
        this.dataSourceType = dataSourceType;
        this.properties = properties;
        this.defaultFileName = defaultFileName;
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
}
