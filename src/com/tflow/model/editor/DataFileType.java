package com.tflow.model.editor;

import com.tflow.model.editor.datasource.DataSourceType;

public enum DataFileType {

    IN_SQL("SQL", "sql.png", DataSourceType.DATABASE, Properties.INPUT_SQL),
    IN_MD("Markdown", "markdown.png", DataSourceType.LOCAL, Properties.INPUT_MARKDOWN),
    IN_ENV("System Environment", "system.png", DataSourceType.SYSTEM, Properties.INPUT_ENVIRONMENT),
    IN_DIR("Directory List", "dir.png", DataSourceType.LOCAL, Properties.INPUT_DIRECTORY),

    OUT_DBINSERT("Insert", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBINSERT),
    OUT_DBUPDATE("Update", "sql.png", DataSourceType.DATABASE, Properties.OUTPUT_DBUPDATE),
    OUT_SQL("SQL", "sql.png", DataSourceType.LOCAL, Properties.OUTPUT_SQL),
    OUT_MD("Markdown", "markdown.png", DataSourceType.LOCAL, Properties.OUTPUT_MARKDOWN),
    OUT_CSV("CSV", "csv.png", DataSourceType.LOCAL, Properties.OUTPUT_CSV),
    OUT_TXT("TXT", "txt.png", DataSourceType.LOCAL, Properties.OUTPUT_TXT),
    ;

    private String name;
    private String image;
    private Properties properties;
    private DataSourceType dataSourceType;

    DataFileType(String name, String image, DataSourceType dataSourceType, Properties properties) {
        this.name = name;
        this.image = image;
        this.dataSourceType = dataSourceType;
        this.properties = properties;
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

    public boolean isInput() {
        return name().substring(0, 1).equals("I");
    }

    public boolean isOutput() {
        return name().substring(0, 1).equals("O");
    }
}
