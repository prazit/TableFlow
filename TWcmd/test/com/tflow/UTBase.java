package com.tflow;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.zookeeper.data.ClientInfo;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UTBase {

    protected String indent = "";
    protected String indentChars = "\t";
    protected boolean printThread = false;

    protected void println(String string) {
        System.out.println(indent + string + (printThread ? " (Thread:" + Thread.currentThread().getName() + ")" : ""));
    }

    protected void indent() {
        indent(1);
    }

    protected void indent(int addIndent) {
        if (addIndent > 0) {
            StringBuilder builder = new StringBuilder(indent);
            for (; addIndent > 0; addIndent--) builder.append(indentChars);
            indent = builder.toString();
            return;
        }
        // addIndex < 0
        int remove = Math.abs(addIndent) * indentChars.length();
        if (remove > indent.length()) {
            indent = "";
        } else {
            indent = indent.substring(0, indent.length() - remove);
        }
    }

    protected void setLogLevel(String logLevel) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = loggerContext.getLoggerList();
        Level level = Level.valueOf(logLevel.toUpperCase());
        loggerList.forEach(tmpLogger -> tmpLogger.setLevel(level));
    }

    protected void setLogLevel(String logLevel, String loggerName) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.exists(loggerName);
        if (logger == null) {
            if (loggerName.compareToIgnoreCase("root") == 0) {
                logger = loggerContext.getLogger(loggerName);
            } else try {
                logger = loggerContext.getLogger(Class.forName(loggerName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        Level level = Level.valueOf(logLevel.toUpperCase());
        logger.setLevel(level);
    }

}
