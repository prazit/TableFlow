package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.DataOutput;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DBMS;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.*;

@ViewScoped
@Named("editorCtl")
public class EditorController extends Controller {

    @Inject
    private Workspace workspace;

    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        FacesUtil.redirect("/editor.xhtml");
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        FacesUtil.redirect("/editor.xhtml");
    }

    @SuppressWarnings("unchecked")
    public void testReadSerialize() {
        List<Action> actionList = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/TestAction.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            actionList = (List<Action>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            log.error("", i);
        } catch (ClassNotFoundException c) {
            log.error("List<Action> class not found", c);
        }

        if (actionList == null) {
            log.error("Action List is Null");
            return;
        }

        for (Action actionBase : actionList) {
            log.info("action = {}", actionBase.toString());
        }
    }

    public void testWriteSerialize() {
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, "String");
        paramMap.put(CommandParamKey.DATA_FILE, "Integer");
        paramMap.put(CommandParamKey.DATA_TABLE, "Date");
        paramMap.put(CommandParamKey.COLUMN_FX, "Decimal");
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, DateTimeUtil.now());
        paramMap.put(CommandParamKey.DATA_TEST1, 35000);
        paramMap.put(CommandParamKey.DATA_TEST2, 35000.00053);

        List<Action> actionList = new ArrayList<>();
        TestAction testAction = new TestAction(paramMap);
        actionList.add(testAction);

        try {
            FileOutputStream fileOut = new FileOutputStream("/Apps/TFlow/TestAction.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(actionList);
            out.close();
            fileOut.close();
            log.info("Serialized data is saved in /Apps/TFlow/TestAction.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void testAction() {
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TEST1, new String[]{"Data1", "Data1.1", "Data1.2"});
        paramMap.put(CommandParamKey.DATA_TEST2, new String[]{"Data2", "Data2.1", "Data2.2"});

        try {
            new TestAction(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("TestAction Failed!", e);
        }
    }

    /**
     * for Mockup Data Only, remove me please.
     */
    private static int testRunningNumber = 0;

    public void addDataSource() {
        /*TODO: need to show parameters dialog and remove Mockup-Data below*/

        Project project = workspace.getProject();
        Step step = project.getStepList().get(project.getActiveStepIndex());

        /*create DataSource, Data File, DataTable (Command: AddDataTable)*/
        Database database = new Database("DB Connection " + (++testRunningNumber), DBMS.ORACLE, project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, database);
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataSource(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataSource Failed!", e);
        }
    }

    private DataTable getSQLDataTable(Project project) {
        /*create DataSource, Data File, DataTable (Commmand: AddDataTable)*/
        Map<String, DataSource> dataSourceList = project.getDataSourceList();
        Database database = (Database) dataSourceList.get(dataSourceList.keySet().toArray()[0]);

        DataFile dataFile = new DataFile(
                DataFileType.IN_SQL,
                "DataFile.sql",
                null,
                project.newElementId(),
                project.newElementId()
        );

        DataTable dataTable = new DataTable(
                1,
                "Mockup Data Table",
                10,
                dataFile,
                database,
                "",
                "String",
                false,
                project.newElementId(),
                project.newElementId()
        );

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String", project.newElementId()));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer", project.newElementId()));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal", project.newElementId()));
        columnList.add(new DataColumn(4, DataType.DATE, "Date", project.newElementId()));

        DataFile outputSQLFile = new DataFile(
                DataFileType.OUT_DBINSERT,
                "account_master",
                null,
                project.newElementId(),
                project.newElementId()
        );

        DataFile outputCSVFile = new DataFile(
                DataFileType.OUT_CSV,
                "output.csv",
                null,
                project.newElementId(),
                project.newElementId()
        );

        Local local = new Local("My Server", "/output/", project.newElementId());

        List<DataOutput> outputList = dataTable.getOutputList();
        outputList.add(new DataOutput(dataTable, outputSQLFile, database, project.newElementId()));
        outputList.add(new DataOutput(dataTable, outputCSVFile, local, project.newElementId()));

        return dataTable;
    }

    /**
     * Create mockup data in activeStep and refresh the flowchart.
     */
    public void addDataTable() {
        /*TODO: need to show parameters dialog and remove Mockup-Data below*/

        Project project = workspace.getProject();
        Step step = project.getStepList().get(project.getActiveStepIndex());

        DataTable dataTable = getSQLDataTable(project);
        /* TODO: need more cases for DataTable (local-file, sftp-file) */

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.TOWER, step.getDataTower());
        paramMap.put(CommandParamKey.LINE, step.getLineList());
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataTable(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataTable Failed!", e);
            /*TODO: show error message on screen*/
            return;
        }

        /*TODO: refresh flowchart page*/
        FacesUtil.runClientScript("refershFlowChart();");
    }

    public void addTransformTable() {
        /*TODO: create TransformTable with ColumnFx*/

        /*TODO: add Output to DataTable and assign as Step Output*/

        /*TODO: add Output to TransformTable and assign as Step Output*/

        /*TODO: refresh flowchart page*/
        FacesUtil.runClientScript("refershFlowChart();");
    }

}
