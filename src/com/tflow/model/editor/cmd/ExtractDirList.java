package com.tflow.model.editor.cmd;

import com.clevel.dconvers.DConvers;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import org.apache.commons.configuration2.Configuration;

public class ExtractDirList extends ExtractCommand {
    @Override
    protected void initProperties(Configuration properties, DConvers dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "dir";
        String dir = System.getProperty("java.home");
        String idColName = "Name";
        String dConversTableId = "table";
        String dConversSourceKey = "source." + dConversTableId;

        properties.addProperty("source", dConversTableId);
        properties.addProperty(dConversSourceKey + ".index", "1");
        properties.addProperty(dConversSourceKey + ".datasource", datasource);
        properties.addProperty(dConversSourceKey + ".query", dir);
        properties.addProperty(dConversSourceKey + ".id", idColName);
    }
}
