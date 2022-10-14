package com.tflow.model.editor.cmd;

import com.clevel.dconvers.conf.Property;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.*;
import com.tflow.util.DConversHelper;
import org.apache.commons.configuration2.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class ExtractSQLInsert extends ExtractCommand {
    @Override
    protected void initProperties(DConversHelper dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "sql";
        String readerName = dataFile.getName();
        String query = Property.READER.key() + readerName;
        String dConversTableId = "sql_insert";

        dConvers.addSourceTable(dConversTableId, 1, datasource, query, "");
        dConvers.addConsoleOutput(dConversTableId);

        Configuration properties = dConvers.getProperties();
        properties.addProperty("source." + dConversTableId + ".query.quotes.name", dataFile.getPropertyMap().get(PropertyVar.quotesName.name()));
        properties.addProperty("source." + dConversTableId + ".query.quotes.value", dataFile.getPropertyMap().get(PropertyVar.quotesValue.name()));

        try {
            BinaryFile binaryFile = project.getManager().loadUploaded(dataFile.getUploadedId(), project);
            dConvers.addReader(readerName, new InputStreamReader(new ByteArrayInputStream(binaryFile.getContent())));
        } catch (ProjectDataException ex) {
            log.error("Load uploaded-file failed! {}", ex.getMessage());
            log.trace("", ex);
        }
    }
}
