package com.tflow.model.editor;

public enum DataFileType {

    IN_SQL("SQL", "sql.png", Properties.INPUT_SQL),
    IN_MD("Markdown", "markdown.png", Properties.INPUT_MARKDOWN),
    IN_ENV("System Environment", "system.png", Properties.INPUT_ENVIRONMENT),
    IN_DIR("Directory List", "dir.png", Properties.INPUT_DIRECTORY),

    OUT_DBINSERT("Insert", "sql.png", Properties.OUTPUT_DBINSERT),
    OUT_DBUPDATE("Update", "sql.png", Properties.OUTPUT_DBUPDATE),
    OUT_SQL("SQL", "sql.png", Properties.OUTPUT_SQL),
    OUT_MD("Markdown", "markdown.png", Properties.OUTPUT_MARKDOWN),
    OUT_CSV("CSV", "csv.png", Properties.OUTPUT_CSV),
    OUT_TXT("TXT", "txt.png", Properties.OUTPUT_TXT),
    ;

    private String name;
    private String image;
    private Properties properties;

    DataFileType(String name, String image, Properties properties) {
        this.name = name;
        this.image = image;
        this.properties = properties;
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

    public boolean isInput() {
        return name().substring(0, 1).equals("I");
    }

    public boolean isOutput() {
        return name().substring(0, 1).equals("O");
    }
}
