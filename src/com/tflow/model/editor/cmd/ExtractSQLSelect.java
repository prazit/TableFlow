package com.tflow.model.editor.cmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.NameValue;
import org.apache.commons.configuration2.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class ExtractSQLSelect extends ExtractCommand {
    @Override
    protected void initProperties(Configuration properties, DConvers dConvers, DataFile dataFile, Step step, Project project) {
        String readerName = dataFile.getName();
        String query = "$[TXT:" + Property.READER.key() + readerName + "]";
        String dConversTableId = "sql_insert";

        String datasource = addDatabaseProperties(properties, dataFile.getDataSourceId(), project);
        addTableProperties(properties, dConversTableId, 0, datasource, query, "");
        addOutputProperties(properties, dConversTableId);
        properties.addProperty("source." + dConversTableId + ".query.quotes.name", dataFile.getPropertyMap().get(PropertyVar.quotesName.name()));
        properties.addProperty("source." + dConversTableId + ".query.quotes.value", dataFile.getPropertyMap().get(PropertyVar.quotesValue.name()));

        String dConversTableMetaId = dConversTableId + "_meta";
        addTableProperties(properties, dConversTableMetaId, 1, "ResultSetMetaData", "SRC:" + dConversTableId, "ColumnLabel");
        addOutputProperties(properties, dConversTableMetaId);

        try {
            BinaryFile binaryFile = project.getManager().loadUploaded(dataFile.getUploadedId(), project);
            dConvers.readerMap.put(readerName, new InputStreamReader(new ByteArrayInputStream(binaryFile.getContent())));
        } catch (ProjectDataException ex) {
            log.error("Load uploaded-file failed! {}", ex.getMessage());
            log.trace("", ex);
        }
    }

    private String addDatabaseProperties(Configuration properties, int dataSourceId, Project project) {
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

    private String getSimpleName(String name) {
        return name.replaceAll("[\\p{Punct}\\s]", "_").replaceAll("_+", "_").toLowerCase();
    }
}
