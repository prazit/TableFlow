package com.tflow.model.editor.cmd;

import com.tflow.model.data.SystemEnvironment;
import com.tflow.model.editor.*;
import com.tflow.util.DConversHelper;

public class ExtractSystemEnvironment extends ExtractCommand {
    @Override
    protected void initProperties(DConversHelper dConvers, DataFile dataFile, Step step, Project project) {
        SystemEnvironment systemEnvironment = SystemEnvironment.parse(dataFile.getName());
        if (systemEnvironment == null) throw new UnsupportedOperationException("Unknown SystemEnvironment '" + dataFile.getName() + "'!");
        DataFileType type = dataFile.getType();
        log.debug("ExtractSystemEnvironment: type({}), systemEnvironment({})", type, systemEnvironment);

        String dConversTableId = systemEnvironment.name().toLowerCase();
        String datasource = "system";
        String query = systemEnvironment.getQuery();
        String idColName = systemEnvironment.getIdColName();

        dConvers.addSourceTable(dConversTableId, 1, datasource, query, idColName);
        dConvers.addConsoleOutput(dConversTableId);

        if (query.toUpperCase().equals("OUTPUT_SUMMARY")) {
            String dConversFirstTableId = "first";
            dConvers.addSourceTable(dConversFirstTableId, 0, datasource, query, idColName);
            dConvers.addConsoleOutput(dConversFirstTableId);
        }
    }
}
