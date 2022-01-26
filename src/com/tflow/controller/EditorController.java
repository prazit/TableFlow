package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.*;
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

    private List<PropertyView> propertyList;
    private Selectable activeObject;

    private boolean showStepList;
    private boolean showPropertyList;

    @PostConstruct
    public void onCreation() {
        initStepList();
    }

    private void initStepList() {
        Project project = workspace.getProject();
        projectName = project.getName();
        refreshStepList(project.getStepList());
        selectStep(project.getActiveStepIndex(), false);
    }

    private void refreshStepList(List<Step> stepList) {
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

    public boolean isShowStepList() {
        return showStepList;
    }

    public void setShowStepList(boolean showStepList) {
        this.showStepList = showStepList;
    }

    public boolean isShowPropertyList() {
        return showPropertyList;
    }

    public void setShowPropertyList(boolean showPropertyList) {
        this.showPropertyList = showPropertyList;
    }
    /*== Public Methods ==*/

    public void log(String msg) {
        log.warn(msg);
    }

    public List<SelectItem> getItemList(PropertyType type, String[] params) throws ClassNotFoundException {
        List<SelectItem> selectItemList = new ArrayList<>();

        Step activeStep = workspace.getProject().getActiveStep();
        Selectable activeObject = activeStep.getActiveObject();

        switch (type) {
            case SYSTEM:
                for (SystemEnvironment value : SystemEnvironment.values()) {
                    selectItemList.add(new SelectItem(value, value.name().replaceAll("[_]", " ")));
                }
                break;

            case CHARSET:
                for (DataCharset value : DataCharset.values()) {
                    selectItemList.add(new SelectItem(value, value.getCharset()));
                }
                break;

            case TXTLENGTHMODE:
                for (TxtLengthMode value : TxtLengthMode.values()) {
                    selectItemList.add(new SelectItem(value, value.name()));
                }
                break;

            case DBMS:
                for (Dbms value : Dbms.values()) {
                    selectItemList.add(new SelectItem(value, value.name()));
                }
                break;

            case SOURCETABLE:
                /* TODO: list all tables before current table */
                /* TODO: remove test list below*/
                for (DataTable dataTable : activeStep.getDataList()) {
                    selectItemList.add(new SelectItem(dataTable.getId(), dataTable.getName()));
                }
                break;

            case COLUMN:
                /* params[0] is property-map-name of table-selectable-id */
                Object tableSelectableId = getPropertyValue(activeObject, params[0]);
                DataTable sourceTable = (DataTable) activeStep.getSelectableMap().get(tableSelectableId.toString());
                if (sourceTable != null) {
                    for (DataColumn sourceColumn : sourceTable.getColumnList()) {
                        selectItemList.add(new SelectItem(sourceColumn.getSelectableId(), sourceColumn.getName()));
                    }
                }
                break;

            case DATASOURCETYPE:
                for (DataSourceType value : DataSourceType.values()) {
                    selectItemList.add(new SelectItem(value, value.name()));
                }
                break;

            case FILETYPE:
                if (params[0].toUpperCase().equals("IN")) {
                    for (DataFileType value : DataFileType.values()) {
                        if (value.isInput()) selectItemList.add(new SelectItem(value, value.getName()));
                    }
                } else {
                    for (DataFileType value : DataFileType.values()) {
                        if (value.isOutput()) selectItemList.add(new SelectItem(value, value.getName()));
                    }
                }
                break;

            case COLUMNFUNCTION:
                for (ColumnFunction value : ColumnFunction.values()) {
                    selectItemList.add(new SelectItem(value, value.getName()));
                }
                break;

            case TABLEFUNCTION:
                for (TableFunction value : TableFunction.values()) {
                    selectItemList.add(new SelectItem(value, value.getName()));
                }
                break;

            case DBCONNECTION:
                Database database;
                for (Map.Entry<Integer, Database> entry : workspace.getProject().getDatabaseMap().entrySet()) {
                    database = entry.getValue();
                    selectItemList.add(new SelectItem(entry.getKey(), database.getName()));
                }
                break;

            case DBTABLE:
                /*TODO: get table list from database connection*/
                break;

            case SFTP:
                SFTP sftp;
                for (Map.Entry<Integer, SFTP> entry : workspace.getProject().getSftpMap().entrySet()) {
                    sftp = entry.getValue();
                    selectItemList.add(new SelectItem(entry.getKey(), sftp.getName()));
                }
                break;

            default:
                log.error("Unknown type({}) to generate item-list", type);
                return selectItemList;
        }

        return selectItemList;
    }

    private Object getPropertyValue(Selectable selectable, String propertyName) {
        Object value = selectable.getPropertyMap().get(propertyName);
        if (value == null) {
            try {
                value = selectable.getClass().getField(propertyName).get(selectable);
            } catch (Exception e) {
                value = "";
                log.error("getPropertyValue(selectable:{}, propertyName:{}) - {}", selectable.getSelectableId(), propertyName, e.getMessage());
            }
        }
        return value == null ? "" : value;
    }

    public List<SelectItem> getColumnList(int dataTableId) {
        List<SelectItem> selectItemList = new ArrayList<>();

        Step activeStep = workspace.getProject().getActiveStep();
        DataTable dataTable = activeStep.getDataTable(dataTableId);
        if (dataTable == null) {
            dataTable = activeStep.getTransformTable(dataTableId);
        }
        if (dataTable == null) {
            log.error("DataTable-ID({}) not found in this step({}:{}), no columns returned by getColumnList", dataTableId, activeStep.getIndex(), activeStep.getName());
            return selectItemList;
        }

        for (DataColumn dataColumn : dataTable.getColumnList()) {
            selectItemList.add(new SelectItem(dataColumn.getName(), dataColumn.getName()));
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
        showStepList = activeStep.isShowStepList();
        showPropertyList = activeStep.isShowPropertyList();

        Selectable activeObject = activeStep.getActiveObject();
        if (activeObject == null) {
            selectObject(null);
        } else {
            selectObject(activeObject.getSelectableId());
        }

        String javascript = "showPropertyList(" + showPropertyList + ");";
        if (refresh) javascript += "refreshFlowChart();";
        FacesUtil.runClientScript(javascript);
    }

    public void submitZoom() {
        String zoom = FacesUtil.getRequestParam("zoom").replaceAll("[%]", "").trim();
        if (zoom.isEmpty()) return;

        Step activeStep = workspace.getProject().getActiveStep();
        log.warn("zoom:{} step:{}", zoom, activeStep.getName());
        activeStep.setZoom(Double.valueOf(zoom));
    }

    public void addStep() {
        Project project = workspace.getProject();
        Step step = new Step("Untitled", project);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddStep(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add Step Failed!", e);
            FacesUtil.addError("Add Step Failed with Internal Command Error!");
            return;
        }

        refreshStepList(project.getStepList());
        selectStep(step.getIndex());

        FacesUtil.addInfo("Step[" + step.getName() + "] added.");
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    public void addDBConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Database database = new Database("Untitled", Dbms.ORACLE, project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, database);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataSource(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add Database Failed!", e);
            FacesUtil.addError("Add Database Failed with Internal Command Error!");
            return;
        }

        selectObject(database.getSelectableId());

        FacesUtil.addInfo("Database[" + database.getName() + "] added.");
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    public void addSFTPConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        SFTP sftp = new SFTP("Untitled", "/", project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, sftp);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataSource(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add SFTP Failed!", e);
            FacesUtil.addError("Add SFTP Failed with Internal Command Error!");
            return;
        }

        selectObject(sftp.getSelectableId());

        FacesUtil.addInfo("SFTP[" + sftp.getName() + "] added.");
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    public void addDataFile() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Local local = new Local("Untitled", "/", project.newElementId());
        DataFile dataFile = new DataFile(local, DataFileType.IN_MD, "Untitled", "/", project.newElementId(), project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, local);
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataFile(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataFile Failed!", e);
            FacesUtil.addError("Add DataFile Failed with Internal Command Error!");
            return;
        }

        selectObject(dataFile.getSelectableId());

        /*FacesUtil.addInfo("DataFile[" + dataFile.getName() + "] added.");
        FacesUtil.runClientScript("refreshFlowChart();");*/
        addDataTable(dataFile);
    }

    private DataTable getDataTable(Project project, DataFile dataFile) {
        /*create DataSource, Data File, DataTable (Commmand: AddDataTable)*/

        DataTable dataTable = new DataTable(
                "Untitled Data Table",
                dataFile,
                "",
                "String",
                false,
                project.newElementId(),
                project.newElementId()
        );

        List<DataColumn> columnList = dataTable.getColumnList();
        columnList.add(new DataColumn(1, DataType.STRING, "String Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(2, DataType.INTEGER, "Integer Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(3, DataType.DECIMAL, "Decimal Column", project.newElementId(), dataTable));
        columnList.add(new DataColumn(4, DataType.DATE, "Date Column", project.newElementId(), dataTable));

        Local myComputer = new Local("MyComputer", "C:/myData/", project.newElementId());
        DataFile outputCSVFile = new DataFile(
                myComputer,
                DataFileType.OUT_CSV,
                "output.csv",
                "out/",
                project.newElementId(),
                project.newElementId()
        );

        List<DataFile> outputList = dataTable.getOutputList();
        outputList.add(outputCSVFile);

        return dataTable;
    }

    /**
     * Create mockup data in activeStep and refresh the flowchart.
     */
    public void addDataTable(DataFile dataFile) {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();
        DataTable dataTable = getDataTable(project, dataFile);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataTable(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataTable Failed!", e);
            FacesUtil.addError("msg", "Add DataTable Failed with Internal Error!");
            return;
        }

        FacesUtil.runClientScript("refreshFlowChart();");
    }

    public void addTransformTable() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        DataTable sourceTable = step.getDataList().get(0);
        TransformTable transformTable = new TransformTable(
                "Transformation Table",
                sourceTable.getId(),
                SourceType.DATA_TABLE,
                sourceTable.getIdColName(),
                project.newElementId(),
                project.newElementId()
        );

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddTransformTable(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add TransformTable Failed!", e);
            FacesUtil.addError("msg", "Add TransformTable Failed with Internal Error!");
            return;
        }

        FacesUtil.runClientScript("refreshFlowChart();");
    }

    private Line getRequestedLine() {
        String startSelectableId = FacesUtil.getRequestParam("startSelectableId");
        if (startSelectableId == null) return null;

        String endSelectableId = FacesUtil.getRequestParam("endSelectableId");
        return new Line(startSelectableId, endSelectableId);
    }

    public void addLine() {
        Line newLine = getRequestedLine();
        addLine(newLine);
    }

    private void addLine(Line newLine) {
        Step step = workspace.getProject().getActiveStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        Selectable startSelectable = selectableMap.get(newLine.getStartSelectableId());
        Selectable endSelectable = selectableMap.get(newLine.getEndSelectableId());

        if (endSelectable instanceof TransformColumn) {
            /*add line from Column to Column*/
            addLookup((DataColumn) startSelectable, (TransformColumn) endSelectable);

        } else if (endSelectable instanceof DataFile) {
            /*add line from DataSource to DataFile*/
            addDataSourceLine((DataSource) startSelectable, (DataFile) endSelectable);

        } else {
            log.error("addLine by unknown types(start:{},end:{})", startSelectable.getClass().getName(), endSelectable.getClass().getName());
        }

        FacesUtil.runClientScript("refreshFlowChart();");
    }

    private void addDataSourceLine(DataSource dataSource, DataFile dataFile) {

    }

    private void addLookup(DataColumn sourceColumn, TransformColumn transformColumn) {
        /*
         * 1. create ColumnFx and add to this step using Action
         * 2. create new line between sourceColumn and columnFx (call addLine again)
         * 3. remain line between columnFx and transformColumn to add by next statements
         */
        Project project = workspace.getProject();

        ColumnFx columnFx = new ColumnFx((DataColumn) transformColumn, ColumnFunction.LOOKUP, "Untitled", project.newElementId(), project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);
        paramMap.put(CommandParamKey.STEP, this);

        try {
            new AddColumnFx(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add ColumnFx Failed!", e);
            FacesUtil.addError("Add ColumnFx Failed with Internal Command Error!");
            return;
        }

        selectObject(columnFx.getSelectableId());

        FacesUtil.addInfo("ColumnFx[" + columnFx.getName() + "] added.");
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

        Step activeStep = workspace.getProject().getActiveStep();
        Selectable activeObject = activeStep.getSelectableMap().get(selectableId);
        if (activeObject == null) {
            log.error("selectableMap not contains selectableId='{}'", selectableId);
            /*throw new IllegalStateException("selectableMap not contains selectableId=" + selectableId);*/
            return;
        }

        activeStep.setActiveObject(activeObject);
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

    public void setToolPanel() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();
        String refresh = FacesUtil.getRequestParam("refresh");
        if (refresh != null) {
            String javascript = "showStepList(" + showStepList + ");"
                    + "showPropertyList(" + showPropertyList + ");";
            FacesUtil.runClientScript(javascript);
            log.warn("setToolPanel(refresh) invoked, runClientScript({})", javascript);
            return;
        }

        String stepList = FacesUtil.getRequestParam("stepList");
        if (stepList != null) {
            showStepList = Boolean.parseBoolean(stepList);
            step.setShowStepList(showStepList);
            refreshStepList(project.getStepList());
            log.warn("setToolPanel(stepList:{}) invoked", showStepList);
        }

        String propertyList = FacesUtil.getRequestParam("propertyList");
        if (propertyList != null) {
            showPropertyList = Boolean.parseBoolean(propertyList);
            step.setShowPropertyList(showPropertyList);
            log.warn("setToolPanel(propertyList:{}) invoked", showPropertyList);
        }
    }

}
