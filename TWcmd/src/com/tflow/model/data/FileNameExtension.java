package com.tflow.model.data;

import org.slf4j.LoggerFactory;

public enum FileNameExtension {

    SQL("sql/"),
    MD("md/"),
    TXT("txt/"),

    XML("conf/", "(\\/)(xml)", "/(\\.|\\/)(xml)$/"),
    CONF("conf/"),
    PROPERTIES("conf/"),

    BAT("bat/"),
    SH("bat/"),

    PNG("resources/images/"),
    CSS("resources/css/"),
    JS("resources/js/"),

    JAVA("src/"),
    CLASS("web/META-INF/classes/"),

    JAR("lib/", "(\\/)(java-archive|zip)", "/(\\.|\\/)(jar)$/"),
    WAR("archive/", "(\\/)(web-archive|zip)", "/(\\.|\\/)(war)$/"),
    ZIP("archive/", "(\\/)(x-zip)", "/(\\.|\\/)(zip)$/"),
    ;

    String buildPath;
    String allowMimeTypes;
    String allowTypes;

    FileNameExtension(String buildPath, String allowMimeTypes, String allowTypes) {
        this.buildPath = buildPath;
        this.allowMimeTypes = allowMimeTypes;
        this.allowTypes = allowTypes;
    }

    FileNameExtension(String buildPath) {
        this.buildPath = buildPath;
        this.allowMimeTypes = "";
        this.allowTypes = "";
    }

    public String getAllowMimeTypes() {
        return allowMimeTypes;
    }

    public String getAllowTypes() {
        return allowTypes;
    }

    public static FileNameExtension forName(String name) {
        if (name == null) return null;

        String[] names = name.split("[.]");
        try {
            return valueOf(names[names.length - 1].toUpperCase());
        } catch (Exception ex) {
            LoggerFactory.getLogger(FileNameExtension.class).warn("No FileNameExtension match for '{}'", name);
            return null;
        }
    }

    public String getBuildPath() {
        return buildPath;
    }
}
