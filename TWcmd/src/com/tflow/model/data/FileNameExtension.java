package com.tflow.model.data;

import org.slf4j.LoggerFactory;

public enum FileNameExtension {

    SQL("sql/"),
    MD("md/"),
    TXT("txt/"),

    XML("xml/"),
    PROPERTY("conf/"),

    BAT("bat/"),
    SH("bat/"),

    PNG("resources/images/"),
    CSS("resources/css/"),
    JS("resources/js/"),

    JAVA("src/"),
    CLASS("web/META-INF/classes/"),

    JAR("lib/"),
    WAR("archive/"),
    ZIP("archive/"),
    ;

    String buildPath;

    FileNameExtension(String buildPath) {
        this.buildPath = buildPath;
    }

    public static FileNameExtension forName(String name) {
        if (name == null) return null;

        String[] names = name.split("[.]");
        try {
            return valueOf(names[names.length - 1]);
        } catch (Exception ex) {
            LoggerFactory.getLogger(FileNameExtension.class).warn("no FileNameExtension match for '{}'", name);
            return null;
        }
    }

    public String getBuildPath() {
        return buildPath;
    }
}
