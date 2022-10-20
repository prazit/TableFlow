package com.tflow.model.editor.cmd;

import com.clevel.dconvers.conf.Property;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.*;
import com.tflow.util.DConversHelper;
import org.apache.commons.configuration2.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class ExtractSQLSelect extends ExtractCommand {
    @Override
    protected void initProperties(DConversHelper dConvers, DataFile dataFile, Step step, Project project) {
        String readerName = dataFile.getName();
        String query = "$[TXT:" + Property.READER.key() + readerName + "]";
        String dConversTableId = "sql_insert";

        String datasource = dConvers.addDatabase(dataFile.getDataSourceId(), project);
        dConvers.addSourceTable(dConversTableId, 0, datasource, query, "");
        dConvers.addConsoleOutput(dConversTableId);

        Configuration properties = dConvers.getProperties();
        properties.addProperty("source." + dConversTableId + ".query.quotes.name", dataFile.getPropertyMap().get(PropertyVar.quotesName.name()));
        properties.addProperty("source." + dConversTableId + ".query.quotes.value", dataFile.getPropertyMap().get(PropertyVar.quotesValue.name()));

        String dConversTableMetaId = dConversTableId + "_meta";
        dConvers.addSourceTable(dConversTableMetaId, 1, "ResultSetMetaData", "SRC:" + dConversTableId, "ColumnLabel");
        dConvers.addConsoleOutput(dConversTableMetaId);

        try {
            BinaryFile binaryFile = project.getManager().loadUploaded(dataFile.getUploadedId(), project);
            dConvers.addReader(readerName, new InputStreamReader(new ByteArrayInputStream(binaryFile.getContent())));
        } catch (ProjectDataException ex) {
            log.error("Load uploaded-file failed! {}", ex.getMessage());
            log.trace("", ex);
        }
    }
}
