package com.tflow.controller;

import com.tflow.model.editor.DataOutput;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DBMS;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("editorCtl")
public class EditorController extends Controller {

    @Inject
    private Workspace workspace;

    private String projectName;
    private MenuModel stepMenu;
    private boolean flowchartEnabled;
    private Double zoom;

    @PostConstruct
    public void onCreation() {
        initStepList();
    }

    private void initStepList() {
        Project project = workspace.getProject();
        List<Step> stepList = project.getStepList();
        projectName = project.getName();

        stepMenu = new DefaultMenuModel();
        List<MenuElement> menuItemList = stepMenu.getElements();
        for (Step step : stepList) {
            menuItemList.add(DefaultMenuItem.builder()
                    .value(step.getName())
                    .icon("pi pi-home")
                    .command("${editorCtl.selectStep(" + step.getIndex() + ")}")
                    .update("actionForm")
                    .build()
            );
        }

        selectStep(project.getActiveStepIndex(), false);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public MenuModel getStepMenu() {
        return stepMenu;
    }

    public void setStepMenu(MenuModel stepMenu) {
        this.stepMenu = stepMenu;
    }

    public boolean isFlowchartEnabled() {
        return flowchartEnabled;
    }

    public void setFlowchartEnabled(boolean flowchartEnabled) {
        this.flowchartEnabled = flowchartEnabled;
    }

    public Double getZoom() {
        return zoom;
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
    }

    /*== Public Methods ==*/

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

    public void selectStep(int stepIndex) {
        selectStep(stepIndex, true);
    }

    public void selectStep(int stepIndex, boolean refresh) {
        Project project = workspace.getProject();
        project.setActiveStepIndex(stepIndex);

        Step activeStep = project.getActiveStep();
        zoom = activeStep.getZoom();

        flowchartEnabled = true;
        if (refresh) FacesUtil.runClientScript("refershFlowChart();");
        log.warn("selectStep(i:{},n:{},z:{})", stepIndex, activeStep.getName(), zoom);
    }

    public void submitZoom() {
        String zoom = FacesUtil.getRequestParam("zoom").replaceAll("[%]", "").trim();
        if (zoom.isEmpty()) return;

        Step activeStep = workspace.getProject().getActiveStep();
        log.warn("zoom:{} step:{}", zoom, activeStep.getName());
        activeStep.setZoom(Double.valueOf(zoom));
    }

    /**
     * for Mockup DataSource Only, remove me please.
     */
    private static int testRunningNumber = 0;

    public void addDataSource() {
        /*TODO: need to show parameters dialog and remove Mockup-Data below*/

        Project project = workspace.getProject();
        Step step = project.getActiveStep();

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
            FacesUtil.addError("Add DataSource Failed with Internal Error!");
            return;
        }

        FacesUtil.addInfo("DataSource[" + database.getName() + "] added.");
        FacesUtil.runClientScript("refershFlowChart();");
    }

    private DataTable getSQLDataTable(Project project) {
        /*create DataSource, Data File, DataTable (Commmand: AddDataTable)*/
        Map<String, DataSource> dataSourceList = project.getDataSourceList();
        Database database = (Database) dataSourceList.get(dataSourceList.keySet().toArray()[0]);

        DataFile dataFile = new DataFile(
                DataFileType.IN_SQL,
                "DataFile.sql",
                "data/",
                project.newElementId(),
                project.newElementId()
        );

        DataTable dataTable = new DataTable(
                project.newUniqueId(),
                "Mockup Data Table",
                dataFile,
                database,
                "",
                "String",
                false,
                project.newElementId(),
                project.newElementId()
        );

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String", project.newElementId(), dataTable));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer", project.newElementId(), dataTable));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal", project.newElementId(), dataTable));
        columnList.add(new DataColumn(4, DataType.DATE, "Date", project.newElementId(), dataTable));

        /*TODO: split code below to the Action AddDataOutput*/
        DataFile outputSQLFile = new DataFile(
                DataFileType.OUT_DBINSERT,
                "accmas",
                "account.",
                project.newElementId(),
                project.newElementId()
        );

        DataFile outputCSVFile = new DataFile(
                DataFileType.OUT_CSV,
                "output.csv",
                "out/",
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
        Step step = project.getActiveStep();

        DataTable dataTable = getSQLDataTable(project);
        /* TODO: need more data-cases for DataTable (local-file, sftp-file) */

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.TOWER, step.getDataTower());
        paramMap.put(CommandParamKey.LINE_LIST, step.getLineList());
        paramMap.put(CommandParamKey.STEP, step);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataTable(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataTable Failed!", e);
            FacesUtil.addError("msg", "Add DataTable Failed with Internal Error!");
            return;
        }

        FacesUtil.runClientScript("refershFlowChart();");
    }

    public void addTransformTable() {
        /*TODO: need to show parameters dialog and remove Mockup-Data below*/

        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        DataTable sourceTable = step.getDataList().get(0);
        TransformTable transformTable = new TransformTable(
                project.newUniqueId(),
                "Transformation Table",
                sourceTable.getId(),
                SourceType.DATA_TABLE,
                sourceTable.getIdColName(),
                project.newElementId(),
                project.newElementId()
        );

        /* TODO: need more source-type for TransformTable (SourceType.TRANSFORM_TABLE) */

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.TOWER, step.getTransformTower());
        paramMap.put(CommandParamKey.LINE_LIST, step.getLineList());
        paramMap.put(CommandParamKey.STEP, step);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddTransformTable(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add TransformTable Failed!", e);
            FacesUtil.addError("msg", "Add TransformTable Failed with Internal Error!");
            return;
        }

        FacesUtil.runClientScript("refershFlowChart();");
    }

}
