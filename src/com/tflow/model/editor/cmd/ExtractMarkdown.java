package com.tflow.model.editor.cmd;

import com.clevel.dconvers.conf.Property;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.util.DConversHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class ExtractMarkdown extends ExtractCommand {
    @Override
    protected void initProperties(DConversHelper dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "markdown";
        String readerName = dataFile.getName();
        String query = Property.READER.key() + readerName;
        String dConversTableId = "markdown";

        dConvers.addSourceTable(dConversTableId, 1, datasource, query, "");
        dConvers.addConsoleOutput(dConversTableId);

        try {
            BinaryFile binaryFile = project.getManager().loadUploaded(dataFile.getUploadedId(), project);
            dConvers.addReader(readerName, new InputStreamReader(new ByteArrayInputStream(binaryFile.getContent())));
        } catch (ProjectDataException ex) {
            log.error("Load uploaded-file failed! {}", ex.getMessage());
            log.trace("", ex);
        }
    }
}
