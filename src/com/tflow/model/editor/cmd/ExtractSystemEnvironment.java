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
public class ExtractSystemEnvironment extends Command {

    private Logger log = LoggerFactory.getLogger(ExtractSystemEnvironment.class);

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        String name = dataFile.getName().split("[.]")[0];
        DataTable dataTable = new DataTable(name, dataFile, "", ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), step);

        String datasource = "environment";
        DataFileType type = dataFile.getType();
        SystemEnvironment systemEnvironment = SystemEnvironment.parse(dataFile.getName());
        if (systemEnvironment == null) {
            throw new UnsupportedOperationException("Unknown SystemEnvironment '" + dataFile.getName() + "'!");
        }
        log.debug("ExtractSystemEnvironment: type({}), systemEnvironment({})", type, systemEnvironment);

        DConvers dConvers = new DConvers(new String[]{
                "--library-mode=preset",
                "--logback=" + getClass().getResource("logback.xml"),
                "--verbose"
        });

        /*create source table configs*/
        String dConversTableId = "table";
        String dConversSourceKey = "source." + dConversTableId;
        Configuration properties = dConvers.dataConversionConfigFile.getProperties();

        properties.addProperty("source", dConversTableId);
        properties.addProperty(dConversSourceKey + ".index", "1");
        properties.addProperty(dConversSourceKey + ".datasource", "system");
        properties.addProperty(dConversSourceKey + ".query", systemEnvironment.getQuery());
        properties.addProperty(dConversSourceKey + ".id", systemEnvironment.getIdColName());

        if (log.isDebugEnabled()) log.debug("source by getList = {}", Arrays.toString(properties.getList("source").toArray()));

        /*start DConvers to create source table by configs above*/
        try {
            dConvers.start();
        } catch (Exception ex) {
            log.debug("dConvers.dataConversionConfigFile={}", dConvers.dataConversionConfigFile);
            throw new UnsupportedOperationException("Unexpected exception from DConvers: ", ex);
        }

        /*got extracted table, get source table by id*/
        Converter converter = dConvers.converterList.get(0);
        Source source = converter.getSource(dConversTableId);
        log.debug("source = {}", source);

        com.clevel.dconvers.data.DataTable extractedTable = converter.getDataTable("SRC:" + dConversTableId);

        /*Copy Columns*/
        List<DataColumn> columnList = dataTable.getColumnList();
        DataRow firstRow = extractedTable.getRow(0);
        for (com.clevel.dconvers.data.DataColumn extractedColumn : firstRow.getColumnList()) {
            columnList.add(new DataColumn(extractedColumn.getIndex(), DataType.parse(extractedColumn.getType()), extractedColumn.getName(), ProjectUtil.newElementId(project), dataTable));
        }

        /*create Default Output as Markdown*/
        OutputFile outputMD = new OutputFile(
                DataFileType.OUT_MD,
                "",
                ProjectUtil.newElementId(project),
                ProjectUtil.newElementId(project)
        );
        List<OutputFile> outputList = dataTable.getOutputList();
        outputList.add(outputMD);

        /*Add to selectableMap*/
        ProjectUtil.generateId(step.getSelectableMap(), dataTable, project);
        ProjectUtil.addTo(step.getSelectableMap(), dataTable, project);

        /*Return result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.DATA_TABLE, dataTable);
    }
}
