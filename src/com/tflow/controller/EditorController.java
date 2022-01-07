package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.Dbms;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
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

    private String projectName;
    private MenuModel stepMenu;
    private Double zoom;

    private Map<String, Selectable> selectableMap;
    private List<PropertyView> propertyList;
    private Selectable activeObject;

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
                    .update("actionForm,propertyForm")
                    .build()
            );
        }

        selectStep(project.getActiveStepIndex(), false);
    }

    private void collectSelectableToMap() {
        Step step = workspace.getProject().getActiveStep();

        List<Selectable> selectableList = step.getDataTower().getSelectableList();
        Selectable activeObject = step.getActiveObject();
        if (activeObject == null && selectableList.size() > 0) {
            activeObject = selectableList.get(0);
            step.setActiveObject(activeObject);
        }

        selectableMap = new HashMap<>();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = step.getTransformTower().getSelectableList();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = step.getOutputTower().getSelectableList();
        collectSelectableTo(selectableMap, selectableList);
    }

    private void collectSelectableTo(Map<String, Selectable> map, List<Selectable> selectableList) {
        for (Selectable selectable : selectableList) {
            map.put(selectable.getSelectableId(), selectable);
            if (selectable instanceof DataTable) {
                DataTable dt = (DataTable) selectable;

                for (DataColumn column : dt.getColumnList()) {
                    map.put(column.getSelectableId(), column);
                }

                for (DataFile output : dt.getOutputList()) {
                    map.put(output.getSelectableId(), output);
                }

                if (selectable instanceof TransformTable) {
                    TransformTable tt = (TransformTable) selectable;
                    for (TableFx fx : tt.getFxList()) {
                        map.put(fx.getSelectableId(), fx);
                    }
                }

            }
        }
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

    public Double getZoom() {
        return zoom;
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
    }

    public List<PropertyView> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<PropertyView> propertyList) {
        this.propertyList = propertyList;
    }

    public Selectable getActiveObject() {
        return activeObject;
    }

    public void setActiveObject(Selectable activeObject) {
        this.activeObject = activeObject;
    }

    /*== Public Methods ==*/

    public void log(String msg) {
        log.warn(msg);
    }

    public List<SelectItem> getItemList(String className) throws ClassNotFoundException {
        List<SelectItem> selectItemList = new ArrayList<>();

        if (className.equals("Dbms")) {
            for (Dbms value : Dbms.values()) {
                selectItemList.add(new SelectItem(value, value.name()));
            }
        }

        return selectItemList;
    }

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
        if (stepIndex < 0 || stepIndex >= project.getStepList().size()) {
            stepIndex = 0;
        }

        int prevStepIndex = project.getActiveStepIndex();
        /*if (prevStepIndex == stepIndex) return;*/

        project.setActiveStepIndex(stepIndex);
        Step activeStep = project.getActiveStep();
        zoom = activeStep.getZoom();

        collectSelectableToMap();

        Selectable activeObject = activeStep.getActiveObject();
        if (activeObject == null) {
            selectObject(null);
        } else {
            selectObject(activeObject.getSelectableId());
        }

        if (refresh) FacesUtil.runClientScript("refershFlowChart();");
    }

    public void submitZoom() {
        String zoom = FacesUtil.getRequestParam("zoom").replaceAll("[%]", "").trim();
        if (zoom.isEmpty()) return;

        Step activeStep = workspace.getProject().getActiveStep();
        log.warn("zoom:{} step:{}", zoom, activeStep.getName());
        activeStep.setZoom(Double.valueOf(zoom));
    }

    public void addDBConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Database database = new Database("Untitled", Dbms.ORACLE, project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, database);
        paramMap.put(CommandParamKey.TOWER, step.getDataTower());
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataSource(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add Database Failed!", e);
            FacesUtil.addError("Add Database Failed with Internal Command Error!");
            return;
        }

        String selectableId = database.getSelectableId();
        selectableMap.put(selectableId, database);
        selectObject(selectableId);

        FacesUtil.addInfo("Database[" + database.getName() + "] added.");
        FacesUtil.runClientScript("refershFlowChart();");
    }

    public void addSFTPConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        SFTP sftp = new SFTP("Untitled", "/", project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, sftp);
        paramMap.put(CommandParamKey.TOWER, step.getDataTower());
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataSource(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add SFTP Failed!", e);
            FacesUtil.addError("Add SFTP Failed with Internal Command Error!");
            return;
        }

        String selectableId = sftp.getSelectableId();
        selectableMap.put(selectableId, sftp);
        selectObject(selectableId);

        FacesUtil.addInfo("SFTP[" + sftp.getName() + "] added.");
        FacesUtil.runClientScript("refershFlowChart();");
    }

    public void addDataFile() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Local local = new Local("Untitled", "/", project.newElementId());
        DataFile dataFile = new DataFile(local, DataFileType.IN_MD, "Untitled", "/", project.newElementId(), project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, local);
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);
        paramMap.put(CommandParamKey.LINE_LIST, step.getLineList());
        paramMap.put(CommandParamKey.TOWER, step.getDataTower());
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.HISTORY, step.getHistory());

        try {
            new AddDataFile(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataFile Failed!", e);
            FacesUtil.addError("Add DataFile Failed with Internal Command Error!");
            return;
        }

        String selectableId = dataFile.getSelectableId();
        selectableMap.put(local.getSelectableId(), local);
        selectableMap.put(selectableId, dataFile);
        selectObject(selectableId);

        FacesUtil.addInfo("DataFile[" + dataFile.getName() + "] added.");
        FacesUtil.runClientScript("refershFlowChart();");
    }

    private DataTable getSQLDataTable(Project project) {
        /*create DataSource, Data File, DataTable (Commmand: AddDataTable)*/
        Map<Integer, Database> databaseMap = project.getDatabaseMap();
        Database database = databaseMap.get(databaseMap.keySet().toArray()[0]);

        DataFile dataFile = new DataFile(
                database,
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
                database,
                DataFileType.OUT_DBINSERT,
                "accmas",
                "account.",
                project.newElementId(),
                project.newElementId()
        );

        Local myComputer = new Local("MyComputer", "C:/myData/", project.newElementId());
        DataFile outputCSVFile = new DataFile(
                myComputer,
                DataFileType.OUT_CSV,
                "output.csv",
                "out/",
                project.newElementId(),
                project.newElementId()
        );

        Local local = new Local("My Server", "/output/", project.newElementId());

        List<DataFile> outputList = dataTable.getOutputList();
        outputList.add(outputSQLFile);
        outputList.add(outputCSVFile);

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

        collectSelectableToMap();
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

        collectSelectableToMap();
        FacesUtil.runClientScript("refershFlowChart();");
    }

    /**
     * Set active object from client script in flowchart.
     */
    public void selectObject() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        selectObject(selectableId);
    }

    public void selectObject(String selectableId) {
        if (selectableId == null) {
            /*reset property sheet variables*/
            setPropertySheet(null);
            return;
        }

        Selectable activeObject = selectableMap.get(selectableId);
        if (activeObject == null) {
            log.error("selectableMap not contains selectableId={}", selectableId);
            /*throw new IllegalStateException("selectableMap not contains selectableId=" + selectableId);*/
            return;
        }

        workspace.getProject().getActiveStep().setActiveObject(activeObject);
        setPropertySheet(activeObject);
    }

    private void setPropertySheet(Selectable activeObject) {
        if (activeObject == null) {
            this.activeObject = null;
            propertyList = new ArrayList<>();
            return;
        }

        this.activeObject = activeObject;
        propertyList = activeObject.getProperties().getPropertyList();
    }

}
