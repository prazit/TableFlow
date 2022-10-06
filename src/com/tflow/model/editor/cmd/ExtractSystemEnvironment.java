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

/**
 * TODO: Need to support all possible queries in SystemEnvironment.java
 */
public class ExtractSystemEnvironment extends ExtractCommand {
    @Override
    protected void initProperties(Configuration properties, DConvers dConvers, DataFile dataFile, Step step, Project project) {
        String datasource = "system";
        DataFileType type = dataFile.getType();
        String dConversTableId = "table";
        String dConversSourceKey = "source." + dConversTableId;
        SystemEnvironment systemEnvironment = SystemEnvironment.parse(dataFile.getName());
        if (systemEnvironment == null) {
            throw new UnsupportedOperationException("Unknown SystemEnvironment '" + dataFile.getName() + "'!");
        }
        log.debug("ExtractSystemEnvironment: type({}), systemEnvironment({})", type, systemEnvironment);

        properties.addProperty("source", dConversTableId);
        properties.addProperty(dConversSourceKey + ".index", "1");
        properties.addProperty(dConversSourceKey + ".datasource", datasource);
        properties.addProperty(dConversSourceKey + ".query", systemEnvironment.getQuery());
        properties.addProperty(dConversSourceKey + ".id", systemEnvironment.getIdColName());
    }
}
