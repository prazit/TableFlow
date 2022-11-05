package com.tflow.util;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.data.DataTable;
import com.clevel.dconvers.ngin.Converter;
import com.tflow.model.data.Dbms;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.datasource.SFTP;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * Notice: please don't make static function in here.
 */
public class DConversHelper {

    private Logger log;

    private DConvers dConvers;
    private Configuration properties;

    public DConversHelper() {
        init(false);
    }

    public DConversHelper(boolean testConnections) {
        init(testConnections);
    }

    private void init(boolean test) {
        log = LoggerFactory.getLogger(getClass());
        String[] args;
        if (test) {
            args = new String[]{
                    "--library-mode=preset",
                    "--logback=" + getClass().getResource("logback.xml"),
                    "--verbose",
                    "--test"
            };
        } else {
            args = new String[]{
                    "--library-mode=preset",
                    "--logback=" + getClass().getResource("logback.xml"),
                    "--verbose"
            };
        }
        this.dConvers = new DConvers(args);
        this.properties = dConvers.dataConversionConfigFile.getProperties();
    }

    public Configuration getProperties() {
        return properties;
    }

    public String getString(String property) {
        return properties.getString(property);
    }

    public DataTable getSourceTable(String tableName) {
        Converter converter = dConvers.converterList.get(0);
        return converter.getDataTable("SRC:" + tableName);
    }

    public String getSimpleName(String name) {
        return new DConversID(name).toString();
    }

    public int getExitCode() {
        return dConvers.currentState.getLongValue().intValue();
    }

    public void addReader(String name, Reader reader) {
        dConvers.readerMap.put(name, reader);
        log.debug("addReader(name:{}, reader:{}) success.", name, reader);
        log.debug("dConvers.readerMap = {}", dConvers.readerMap);
    }

    public String addDatabase(int dataSourceId, Project project) {
        Database database = project.getDatabaseMap().get(dataSourceId);
        if (database == null) {
            return "unknown_database_id_" + dataSourceId;
        }

        /* example from GoldSpot Migration
            datasource=oldsystem
            datasource.oldsystem.url=jdbc:mysql://35.197.155.235:3306
            datasource.oldsystem.driver=com.mysql.jdbc.Driver
            datasource.oldsystem.schema=goldspot_prod
            datasource.oldsystem.user=remote
            datasource.oldsystem.password=ktSgu3w6fmF6Ieviu65wCmm4r
            datasource.oldsystem.prop.zeroDateTimeBehavior=convertToNull
            datasource.oldsystem.prop.useUnicode=true
            datasource.oldsystem.prop.characterEncoding=utf8
            datasource.oldsystem.prop.characterSetResults=utf8
            datasource.oldsystem.prop.autoReconnect=true
        * */
        String name = getSimpleName(database.getName());
        String datasource = "datasource." + name + ".";

        Dbms dbms = database.getDbms();
        String schema = database.getSchema();
        String url = dbms.getURL(database.getHost(), database.getPort(), schema);

        properties.addProperty("datasource", name);
        properties.addProperty(datasource + "url", url);
        properties.addProperty(datasource + "driver", dbms.getDriverName());
        if (schema != null && !schema.isEmpty() && !url.contains(schema)) properties.addProperty(datasource + "schema", schema);
        properties.addProperty(datasource + "user", database.getUser());
        properties.addProperty(datasource + "password", database.getPassword());
        properties.addProperty(datasource + "user.encrypted", "false");
        properties.addProperty(datasource + "password.encrypted", "false");

        for (NameValue nameValue : database.getPropList()) {
            properties.addProperty(datasource + "prop." + nameValue.getName(), nameValue.getValue());
        }

        return name;
    }

    public String addSFTP(int sftpId, Project project) {
        SFTP sftp = project.getSftpMap().get(sftpId);
        if (sftp == null) {
            return "unknown_sftp_id_" + sftpId;
        }

        /* example from ETL
            sftp=sftpserver
            sftp.sftpserver.host=w2sftpdho101
            sftp.sftpserver.port=22
            sftp.sftpserver.user=sftpcbs
            sftp.sftpserver.password=sftpcbs123
            sftp.sftpserver.retry=3
            sftp.sftpserver.tmp=ifrs9/$[CAL:GET(SRC:header,VARIABLE=TODAY,VALUE)]/downloaded/
        * */
        String name = getSimpleName(sftp.getName());
        String sftpKey = "sftp." + name + ".";

        properties.addProperty("sftp", name);
        properties.addProperty(sftpKey + "host", sftp.getHost());
        properties.addProperty(sftpKey + "port", sftp.getPort());
        properties.addProperty(sftpKey + "user", sftp.getUser());
        properties.addProperty(sftpKey + "password", sftp.getPassword());
        properties.addProperty(sftpKey + "retry", "false");
        properties.addProperty(sftpKey + "tmp", "false");

        return name;
    }

    public void addSourceTable(String tableName, int tableIndex, String datasource, String query, String idColumnName) {
        properties.addProperty("source", tableName);
        String dConversSourceKey = "source." + tableName;
        properties.addProperty(dConversSourceKey + ".index", String.valueOf(tableIndex));
        properties.addProperty(dConversSourceKey + ".datasource", datasource);
        properties.addProperty(dConversSourceKey + ".query", query);
        properties.addProperty(dConversSourceKey + ".id", idColumnName);
    }

    public void addConsoleOutput(String sourceTableName) {
        String prefix = "source." + sourceTableName + ".markdown";
        properties.addProperty(prefix, "true");
        properties.addProperty(prefix + ".output", "console");
        properties.addProperty(prefix + ".mermaid", "false");
        properties.addProperty(prefix + ".comment", "false");
        properties.addProperty(prefix + ".comment.datasource", "true");
        properties.addProperty(prefix + ".comment.query", "true");
        properties.addProperty(prefix + ".title", "false");
    }

    public boolean run() {
        try {
            dConvers.start();
            if (getExitCode() == dConvers.dataConversionConfigFile.getErrorCode()) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public void printProperties(Logger log) {
        Iterator<String> keyList = properties.getKeys();
        StringBuilder msg = new StringBuilder();
        while (keyList.hasNext()) {
            String key = keyList.next();
            if (key.equals("source")) {
                for (Object source : properties.getList(key)) {
                    msg.append(",'source':'").append(source).append("'");
                }
            } else {
                msg.append(",'").append(key).append("':'").append(properties.getString(key)).append("'");
            }
        }
        msg.setCharAt(0, '{');
        msg.append("}");
        log.debug("DConvers-Properties: {}", msg.toString());
    }

    public void addVariable(String name, String value) {
        properties.addProperty("variable." + name, value);
    }
}
