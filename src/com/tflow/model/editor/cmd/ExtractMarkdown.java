package com.tflow.model.editor.cmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.drew.lang.ByteArrayReader;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import org.apache.commons.configuration2.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ExtractMarkdown extends ExtractCommand {
    @Override
    protected void initProperties(Configuration properties, DConvers dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "markdown";
        String readerName = dataFile.getName();
        String query = Property.READER.key() + readerName;
        String dConversTableId = "table";

        addTableProperties(properties, dConversTableId, 1, datasource, query, "");
        addOutputProperties(properties, dConversTableId);

        try {
            BinaryFile binaryFile = project.getManager().loadUploaded(dataFile.getUploadedId(), project);
            dConvers.readerMap.put(readerName, new InputStreamReader(new ByteArrayInputStream(binaryFile.getContent())));
        } catch (ProjectDataException ex) {
            log.error("Load uploaded-file failed! {}", ex.getMessage());
            log.trace("", ex);
        }
    }
}
