package com.tflow.model.editor.cmd;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.conf.Property;
import com.clevel.dconvers.conf.SourceConfig;
import com.clevel.dconvers.data.DataRow;
import com.clevel.dconvers.ngin.Converter;
import com.clevel.dconvers.ngin.Source;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.util.DConversHelper;
import com.tflow.util.ProjectUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
