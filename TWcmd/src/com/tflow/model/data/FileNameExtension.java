package com.tflow.model.data;

import org.slf4j.LoggerFactory;

public enum FileNameExtension {

    SQL,
    MD,
    TXT,

    XML,
    PROPERTY,

    BAT,
    SH,

    PNG,
    CSS,
    JS,

    JAVA,
    CLASS,

    JAR,
    WAR,
    ZIP,
    ;

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
}
