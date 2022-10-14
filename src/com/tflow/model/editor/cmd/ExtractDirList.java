package com.tflow.model.editor.cmd;

import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.util.DConversHelper;

public class ExtractDirList extends ExtractCommand {
    @Override
    protected void initProperties(DConversHelper dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "dir";
        String dir = System.getProperty("java.home");
        String idColName = "Name";
        String dConversTableId = "dir_list";

        dConvers.addSourceTable(dConversTableId, 1, datasource, dir, idColName);
        dConvers.addConsoleOutput(dConversTableId);
    }
}
