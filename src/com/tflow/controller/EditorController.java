package com.tflow.controller;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.kafka.KafkaRecord;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.PageParameter;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.data.SystemEnvironment;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.view.ActionView;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.editor.view.VersionedFile;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.model.mapper.RecordMapper;
import com.tflow.util.DConversHelper;
import com.tflow.util.FacesUtil;
import com.tflow.util.ProjectUtil;
import com.tflow.util.SerializeUtil;
import net.mcmanus.eamonn.serialysis.SEntity;
import net.mcmanus.eamonn.serialysis.SerialScan;
import org.apache.tika.Tika;
import org.mapstruct.factory.Mappers;
import org.primefaces.component.chips.Chips;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@ViewScoped
@Named("editorCtl")
public class EditorController extends Controller {

    private String projectName;
    private MenuModel stepMenu;
    private Double zoom;

    private List<ActionView> actionList;

    private EditorType editorType;

    private List<PropertyView> propertyList;
    private Selectable activeObject;

    private String leftPanelTitle;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private boolean showColumnNumbers;
    private int stepListActiveTab;

    private Map<String, Integer> actionPriorityMap;
    private boolean fullActionList;

    private boolean focusOnLastProperties;

    @Override
    public Page getPage() {
        return Page.EDITOR;
    }

    @Override
    public void onCreation() {
        leftPanelTitle = "Step List";
        focusOnLastProperties = false;

        /*Open Editor Cases.
         * 1. hasParameter(GroupId, ProjectId/TemplateId): New Project from Template/Existing Project
         * 2. hasParameter(ProjectId): Open Project
         * 3. hasParameter(GroupId): New Empty Project
         * 4. noParameter: Invalid when WorkSpace.Project is null
         * 5. noParameter: Normal Working when WorkSpace.Project not null
         **/
        Map<PageParameter, String> parameterMap = workspace.getParameterMap();
        String projectId = parameterMap.get(PageParameter.PROJECT_ID);
        String groupId = parameterMap.get(PageParameter.GROUP_ID);
        if (projectId != null && groupId != null) {
            log.info("Open-Page:Editor: New Project from Template/Existing Project {} into Group {}", projectId, groupId);
            parameterMap.clear();
            String newProjectId = createNewProject(Integer.parseInt(groupId), projectId);
            if (newProjectId == null) {
                workspace.openPage(Page.GROUP);
                return;
            }

            /*new project from template need to load new project after create*/
            if (!openProject(newProjectId)) {
                jsBuilder.pre(JavaScript.notiError, "Open project(" + newProjectId + ") failed!");
                workspace.openPage(Page.GROUP);
                return;
            }

            /*to support client refresh page will go in case 5.*/
            parameterMap.clear();

        } else if (projectId != null) {
            log.info("Open-Page:Editor: Open Project {}", projectId);
            if (!openProject(projectId)) {
                jsBuilder.pre(JavaScript.notiError, "Open project(" + projectId + ") failed!");
                workspace.openPage(Page.GROUP);
                return;
            }

        } else if (groupId != null) {
            log.info("Open-Page:Editor: Create New Empty Project into Group {}", groupId);
            parameterMap.clear();
            String newProjectId = createNewProject(Integer.parseInt(groupId));
            if (newProjectId == null) {
                workspace.openPage(Page.GROUP);
                return;
            }

            /*to support client refresh page will go in case 5.*/
            parameterMap.clear();

        } else if (workspace.getProject() == null) {
            log.warn("Open-Page:Editor: Required Parameter: GroupID, ProjectID.");
            workspace.openPage(Page.GROUP);
            return;

        } else { /*case 5. workspace.project is not null*/
            log.info("Open-Page:Editor: Normal/Refresh");
        }

        setEditorType(EditorType.STEP);
        initActionPriorityMap();
        initStepList();
        //selectProject();
    }

    public void preRenderComponent() {
        log.warn("preRenderComponent: javaScriptBuilder={}", jsBuilder);
        jsBuilder.runOnClient(true);
    }

    private void initActionPriorityMap() {
        actionPriorityMap = new HashMap<>();
        actionPriorityMap.put("RML", 1);
        actionPriorityMap.put("AML", 2);
    }

    private void initStepList() {
        Project project = workspace.getProject();
        selectStep(project.getActiveStepIndex(), false);
        refreshStepList(project.getStepList());
    }

    public void refreshActionList() {
        refreshActionList(workspace.getProject());
    }

    private void refreshActionList(Project project) {
        Step step = project.getActiveStep();
        if (step == null) return;

        /*need to group more chains to one action*/
        actionList = new ArrayList<>();
        if (fullActionList) {
            for (Action action : step.getHistory()) {
                actionList.add(new ActionView(action));
            }
        } else {
            Action currentAction = null;
            int currentPriority = 0;
            for (Action action : step.getHistory()) {
                int actionPriority = getActionPriority(action);
                if (currentAction == null || actionPriority > currentPriority) {
                    currentAction = action;
                    currentPriority = actionPriority;
                }
                if (action.getNextChain() == null) {
                    ActionView view = new ActionView(currentAction);
                    view.setId(action.getId()); //need to use id from the last action of a group (more detailed, see: undo function).
                    actionList.add(view);
                    currentAction = null;
                    currentPriority = 0;
                }
            }
        }
    }

    private int getActionPriority(Action action) {
        Integer priority = actionPriorityMap.get(action.getCode());
        if (priority == null) {
            return 99;
        }
        return priority;
    }

    private void refreshStepList(List<Step> stepList) {
        projectName = workspace.getProject().getName();
        stepMenu = new DefaultMenuModel();
        List<MenuElement> menuItemList = stepMenu.getElements();

        menuItemList.add(DefaultMenuItem.builder()
                .value("Project: " + projectName)
                .icon("pi pi-home")
                .command("${editorCtl.selectStep(-1)}")
                .update("actionForm,propertyForm")
                .onclick("blockScreen('PROJECT PAGE LOADING');")
                .oncomplete("unblockScreen();")
                .build()
        );

        int index = 0;
        for (Step step : stepList) {
            menuItemList.add(DefaultMenuItem.builder()
                    .value(step.getName().isEmpty() ? "Step: Untitled" : step.getName())
                    .icon("pi pi-play")
                    .command("${editorCtl.selectStep(" + (index++) + ")}")
                    .update("actionForm,propertyForm")
                    .onclick("blockScreen('" + step.getName().toUpperCase() + " STEP LOADING');")
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

    public List<ActionView> getActionList() {
        return actionList;
    }

    public void setActionList(List<ActionView> actionList) {
        this.actionList = actionList;
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

    public boolean isShowActionButtons() {
        return showActionButtons;
    }

    public void setShowActionButtons(boolean showActionButtons) {
        this.showActionButtons = showActionButtons;
    }

    public boolean isShowColumnNumbers() {
        return showColumnNumbers;
    }

    public void setShowColumnNumbers(boolean showColumnNumbers) {
        this.showColumnNumbers = showColumnNumbers;
    }

    public String getLeftPanelTitle() {
        return leftPanelTitle;
    }

    public void setLeftPanelTitle(String leftPanelTitle) {
        this.leftPanelTitle = leftPanelTitle;
    }

    public int getStepListActiveTab() {
        return stepListActiveTab;
    }

    public void setStepListActiveTab(int stepListActiveTab) {
        this.stepListActiveTab = stepListActiveTab;
    }

    public boolean isFullActionList() {
        return fullActionList;
    }

    public void setFullActionList(boolean fullActionList) {
        this.fullActionList = fullActionList;
    }

    public void setEditorType(EditorType editorType) {
        this.editorType = editorType;
    }

    public EditorType getEditorType() {
        return editorType;
    }

    public boolean isFocusOnLastProperties() {
        boolean trueOfFalse = this.focusOnLastProperties;
        focusOnLastProperties = false;
        return trueOfFalse;
    }

    /*== Public Methods ==*/

    public void log(String msg) {
        log.warn(msg);
    }

    public List<SelectItem> getItemList(PropertyView propertyView) throws ClassNotFoundException, ClassCastException {
        PropertyType type = propertyView.getType();
        String[] params = propertyView.getParams();

        List<SelectItem> selectItemList = new ArrayList<>();

        Project project = workspace.getProject();
        Step activeStep = project.getActiveStep();
        Selectable activeObject = activeStep.getActiveObject();
        Properties properties = activeObject.getProperties();
        ProjectFileType activeObjectType = activeObject.getProjectFileType();
        if (log.isDebugEnabled()) log.debug("getItemList(property:{}, activeStep:{}, activeObject:({}){}.", propertyView, activeStep.getSelectableId(), activeObject.getClass().getSimpleName(), activeObject);

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
                DataTable activeDataTable = getActiveDataTable(activeObject);
                if (activeDataTable == null) break;

                int level = activeDataTable.getLevel();
                for (DataTable dataTable : activeStep.getDataList()) {
                    if (dataTable.getLevel() >= level) /*list all tables who appear before current table only*/ continue;
                    selectItemList.add(new SelectItem(dataTable.getSelectableId(), dataTable.getName()));
                }
                break;

            case COLUMN:
                /*Notice: valid only for DataColumn, DataTable*/
                /* params[0] is property-name that contains id of source-table*/
                Object tableId;
                DataTable sourceTable;
                if (ProjectFileType.DATA_TABLE == activeObjectType || ProjectFileType.TRANSFORM_TABLE == activeObjectType) {
                    tableId = properties.getPropertyValue(activeObject, params[0], log);
                } else { /*ProjectFileType.DATA_COLUMN || ProjectFileType.TRANSFORM_COLUMN*/
                    DataColumn dataColumn = (DataColumn) activeObject;
                    tableId = properties.getPropertyValue(dataColumn.getOwner(), params[0], log);
                }
                sourceTable = activeStep.getDataTable((Integer) tableId);
                if (sourceTable == null) {
                    sourceTable = activeStep.getTransformTable((Integer) tableId);
                }
                if (sourceTable != null) {
                    boolean useId = params[1].toUpperCase().equals("ID");
                    for (DataColumn sourceColumn : sourceTable.getColumnList()) {
                        selectItemList.add(new SelectItem((useId ? sourceColumn.getId() : sourceColumn.getName()), sourceColumn.getName()));
                    }
                }
                break;

            case DATASOURCE:
                /*Notice: found used in STEP_DATA_SOURCE, INPUT_XXX, OUTPUT_XXX */
                String dataSourceType = null;
                int paramCount = params.length;
                if (paramCount > 0 && !params[0].isEmpty()) {
                    dataSourceType = params[0].toUpperCase();
                } else if (paramCount > 1 && !params[1].isEmpty()) {
                    dataSourceType = ((DataSourceType) properties.getPropertyValue(activeObject, params[1], log)).name();
                }
                if (dataSourceType == null) dataSourceType = DataSourceType.DATABASE.name() + DataSourceType.LOCAL.name() + DataSourceType.SFTP.name();

                if (dataSourceType.contains(DataSourceType.DATABASE.name())) for (Database database : project.getDatabaseMap().values()) {
                    selectItemList.add(new SelectItem(DataSourceType.DATABASE + ":" + database.getId(), database.getDbms() + ":" + database.getName()));
                }
                if (dataSourceType.contains(DataSourceType.LOCAL.name())) for (Local local : project.getLocalMap().values()) {
                    selectItemList.add(new SelectItem(DataSourceType.LOCAL + ":" + local.getId(), local.getName() + ":" + local.getRootPath()));
                }
                if (dataSourceType.contains(DataSourceType.SFTP.name())) for (SFTP sftp : project.getSftpMap().values()) {
                    selectItemList.add(new SelectItem(DataSourceType.SFTP + ":" + sftp.getId(), sftp.getName() + ":" + sftp.getRootPath()));
                }
                if (dataSourceType.contains(DataSourceType.SYSTEM.name())) for (SystemEnvironment sys : SystemEnvironment.values()) {
                    selectItemList.add(new SelectItem(DataSourceType.SYSTEM + ":" + sys.getId(), sys.name().replaceAll("[_]", " ")));
                }
                if (dataSourceType.contains(DataSourceType.FIXED.name())) selectItemList.add(new SelectItem(DataSourceType.FIXED + ":0", "Static File"));
                break;

            case DATASOURCETYPE:
                for (DataSourceType value : DataSourceType.values()) {
                    selectItemList.add(new SelectItem(value, value.name()));
                }
                break;

            case DATAFILETYPE:
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

    private DataTable getActiveDataTable(Selectable activeObject) {
        /*need to support SOURCETABLE on ColumnFx, DataColumn, TableFx and DataFile(output)*/
        if (activeObject instanceof DataTable) return (DataTable) activeObject;
        if (activeObject instanceof ColumnFx) return ((ColumnFx) activeObject).getOwner().getOwner();
        if (activeObject instanceof DataColumn) return ((DataColumn) activeObject).getOwner();
        if (activeObject instanceof TableFx) return ((TableFx) activeObject).getOwner();
        if (activeObject instanceof DataFile) return (DataTable) ((DataFile) activeObject).getOwner();
        return null;
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

    public void testSaveProjectTemplate() {
        log.info("testSaveProject: started");

        Project project = workspace.getProject();
        project.getManager().saveProjectAs("P1", project);

        log.info("testSaveProject: completed");
    }

    public boolean openProject(String projectId) {
        try {
            ProjectManager projectManager = new ProjectManager(workspace.getEnvironment());
            projectManager.loadProject(workspace, projectId);
        } catch (Exception ex) {
            log.error("openProject: error from server(" + ex.getMessage() + ")");
            log.trace("", ex);
            return false;
        }

        log.info("open project({}) success Project={}", projectId, workspace.getProject());
        return true;
    }

    private void testConvertByteArrayAndString(KafkaRecord kafkaRecord) {
        /*#1 using ByteStream*/
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(kafkaRecord);
            objectOutputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            KafkaRecord deserilizedKafkaRecord = (KafkaRecord) objectInputStream.readObject();
            objectInputStream.close();
            log.warn("testConvertByteArrayAndString: #1 serilizedKafkaRecordValue: {}", Arrays.toString(byteArrayOutputStream.toByteArray()));
            log.warn("testConvertByteArrayAndString: #1 deserilizedKafkaRecordValue: {}", deserilizedKafkaRecord);

            String value = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
            byteArrayInputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.ISO_8859_1));
            log.warn("testConvertByteArrayAndString: #2 serilizedKafkaRecordValue: {}", Arrays.toString(value.getBytes()));
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            deserilizedKafkaRecord = (KafkaRecord) objectInputStream.readObject();
            objectInputStream.close();
            log.warn("testConvertByteArrayAndString: #2 deserilizedKafkaRecordValue: {}", deserilizedKafkaRecord);

        } catch (Exception ex) {
            log.error("testConvertByteArrayAndString: " + ex.getMessage());
            log.trace("", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void testReadActionList() {
        /*Notice: serialized file can contains Footer-Data after serialized-data without any concern.*/
        /*Notice: serialized file can contains Header-Data before serialized-data when use SerialScan.readObject to read.*/
        /*Notice: IMPORTANT: serialized file can't contains Header-Data before serialized-data when use Object.readObject to read.*/
        List<Action> actionList = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/tmp/TestSerialize.ser");

            /*-- normal cast to known object --*/
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

    public String testReadFromFile() {
        String value = "";
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/tmp/TestSerialize.ser");

            /*-- normal cast to known object --*/
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader inputStreamReader = new InputStreamReader(fileIn, StandardCharsets.US_ASCII);
            log.warn("testReadFromFile: inputStreamReader.getEncoding: {}", inputStreamReader.getEncoding());
            int ch = inputStreamReader.read();
            while (ch != -1) {
                stringBuilder.append((char) ch);
                ch = inputStreamReader.read();
            }
            inputStreamReader.close();
            fileIn.close();
            value = stringBuilder.toString();

        } catch (Exception ex) {
            log.error("testReadFromFile: " + ex.getMessage());
            log.trace("", ex);
        }

        return value;
    }

    public void testReadKafkaRecordValue() {
        KafkaRecord kafkaRecord = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/tmp/TestSerialize.ser");

            /*-- normal cast to known object --*/
            ObjectInputStream in = new ObjectInputStream(fileIn);
            kafkaRecord = (KafkaRecord) in.readObject();
            in.close();

            fileIn.close();
        } catch (IOException i) {
            log.error("", i);
        } catch (ClassNotFoundException c) {
            log.error("List<Action> class not found", c);
        }

        if (kafkaRecord == null) {
            log.error("KafkaRecordValue is Null");
            return;
        }

        log.info("kafkaRecordValue = {}", kafkaRecord.toString());
    }

    public void testScanSerialize() {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream("/Apps/TFlow/tmp/TestSerialize.ser");

            /*-- scan for serialized object for unknown object --*/
            SerialScan serialScan = new SerialScan(fileIn);
            SEntity sEntity = serialScan.readObject();
            int objectCount = 0;
            while (sEntity != null) {
                objectCount++;
                log.warn("sEntity[{}]={}", objectCount, sEntity);
                sEntity = serialScan.readObject();
            }

            fileIn.close();

        } catch (EOFException eof) {
            try {
                fileIn.close();
            } catch (IOException e) {
                /*nothing*/
            }

        } catch (Exception e) {
            log.error("testScanSerialize failed, ", e);
        }
    }

    public void testWriteActionList() {
        List<Action> history = workspace.getProject().getActiveStep().getHistory();
        testWriteSerialize(history, null, null);
    }

    public void testWriteKafkaRecordValue() {
        List<Action> history = workspace.getProject().getActiveStep().getHistory();
        KafkaRecordAttributes additional = new KafkaRecordAttributes();
        additional.setProjectId(workspace.getProject().getName());
        additional.setClientId(3);
        additional.setUserId(23);
        KafkaRecord kafkaRecord = new KafkaRecord(history, additional);
        testWriteSerialize(kafkaRecord, null, null);
    }

    public void testWriteHeader() {
        List<Action> history = workspace.getProject().getActiveStep().getHistory();
        testWriteSerialize(history, "TFlow - Some header before real data.", null);
    }

    public void testWriteFooter() {
        List<Action> history = workspace.getProject().getActiveStep().getHistory();
        testWriteSerialize(history, null, "TFlow - Some footer after real data.");
    }

    public void testWriteSerialize(Object object, String header, String footer) {
        testWriteSerialize(object, header, footer, null);
    }

    public void testWriteSerialize(Object object, String header, String footer, String fileName) {
        try {
            if (fileName == null) {
                fileName = "/Apps/TFlow/tmp/TestSerialize.ser";
            }

            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            if (header != null) {
                out.writeChars(header);
            }
            if (object instanceof String) {
                out.write(((String) object).getBytes(StandardCharsets.ISO_8859_1));
            } else {
                out.writeObject(object);
            }
            if (footer != null) {
                out.writeChars(footer);
            }
            out.close();
            fileOut.close();
            log.info("Serialized data is saved in /Apps/TFlow/tmp/TestSerialize.ser");
        } catch (IOException i) {
            log.error("testWriteSerialize failed,", i);
        }
    }

    public void testToJson() {
        Project project = workspace.getProject();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        RecordMapper recordMapper = Mappers.getMapper(RecordMapper.class);

        KafkaRecordAttributes kafkaRecordAttributes = new KafkaRecordAttributes();
        kafkaRecordAttributes.setProjectId("P1");
        kafkaRecordAttributes.setRecordId("P1");
        kafkaRecordAttributes.setClientId(workspace.getClient().getId());
        kafkaRecordAttributes.setUserId(workspace.getUser().getId());

        String data = new String(SerializeUtil.toTJson(mapper.map(project)), StandardCharsets.ISO_8859_1);

        KafkaRecord kafkaRecord = new KafkaRecord();
        kafkaRecord.setData(data);
        kafkaRecord.setAdditional(kafkaRecordAttributes);

        log.warn("testToJson: serializing...");
        String json = new String(SerializeUtil.toTJson(kafkaRecord), StandardCharsets.ISO_8859_1);
        log.warn("testToJson: serialized = \n{}", json);
    }

    public void testFromJson() {
        //String json = "com.tflow.model.data.ProjectData={\"id\":\"P1\",\"name\":\"Mockup Project\",\"activeStepIndex\":0,\"lastElementId\":0,\"lastUniqueId\":0}";
        String json = "com.tflow.kafka.KafkaRecordValue={\"data\":\"com.tflow.model.data.ProjectData\\u003d{\\\"id\\\":\\\"P1\\\",\\\"name\\\":\\\"Mockup Project\\\",\\\"activeStepIndex\\\":0,\\\"lastElementId\\\":0,\\\"lastUniqueId\\\":0}\",\"additional\":\"com.tflow.model.data.AdditionalData\\u003d{\\\"recordId\\\":\\\"P1\\\",\\\"projectId\\\":\\\"P1\\\",\\\"modifiedClientId\\\":1,\\\"modifiedUserId\\\":1,\\\"createdClientId\\\":0,\\\"createdUserId\\\":0}\"}";
        Object object = null;
        try {
            object = SerializeUtil.fromTJson(json.getBytes(StandardCharsets.ISO_8859_1));
            log.warn("testFromJson: object = \n{}", object);
        } catch (Error | Exception ex) {
            log.error("testFromJson: object = null with error: " + ex.getMessage());
            log.trace("", ex);
        }
    }

    public void selectStep(int stepIndex) {
        log.warn("selectStep:fromClient(stepIndex:{})", stepIndex);
        selectStep(stepIndex, true);
    }

    /**
     * @param stepIndex -1 = selectProject, 0,1,2,.. = selectStep
     * @param refresh   true will call refreshFlowchart
     */
    private void selectStep(int stepIndex, boolean refresh) {
        boolean isSelectProject = stepIndex < 0;
        setEditorType(isSelectProject ? EditorType.PROJECT : EditorType.STEP);

        Project project = workspace.getProject();
        StepList<Step> stepList = project.getStepList();
        int size = stepList.size();
        if (stepIndex >= size) {
            log.warn("selectStep({}) invalid stepIndex, stepList.size={}, reset stepIndex to 0", stepIndex, size);
            stepIndex = 0;
        }

        /*Auto Add First Step*/
        boolean needEventHandler = false;
        Step step = null;
        try {
            step = stepList.get(stepIndex);
            if (step.getIndex() < 0) needEventHandler = true;
        } catch (IndexOutOfBoundsException ex) {
            if (stepIndex == 0) {
                log.warn("selectStep(0) on new project, then call addStep().");
                step = addStep();
                needEventHandler = true;
            } else if (isSelectProject) {
                step = new Step("for SelectProject from StepList", project);
                step.setIndex(-1);
                stepList.addNegativeItem(step);
            }
        }

        /*call action SelectStep*/
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.INDEX, stepIndex);

        try {
            Action action = new SelectStep(paramMap);
            action.execute();
            step = (Step) action.getResultMap().get(ActionResultKey.STEP);
        } catch (RequiredParamException e) {
            log.error("Select Step Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Select Step Failed with Internal Command Error!");
            return;
        }

        if (needEventHandler) createStepEventHandlers(step);

        zoom = step.getZoom();
        showStepList = step.isShowStepList();
        showPropertyList = step.isShowPropertyList();
        showActionButtons = step.isShowActionButtons();
        showColumnNumbers = step.isShowColumnNumbers();
        stepListActiveTab = step.getStepListActiveTab();

        Selectable activeObject = step.getActiveObject();
        if (isSelectProject) {
            selectObject(project.getSelectableId());
        } else if (activeObject == null || activeObject instanceof DataSource /*|| activeObject instanceof PackageFile*/) {
            selectObject(step.getSelectableId());
        } else {
            selectObject(activeObject.getSelectableId());
        }

        refreshActionList(project);

        if (refresh) {
            jsBuilder
                    .pre(JavaScript.setFlowChart, editorType.getPage())
                    .post(JavaScript.refreshFlowChart)
                    .runOnClient();
        }
    }

    private void createStepEventHandlers(Step step) {
        step.getEventManager().addHandler(EventName.NAME_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                Step target = (Step) event.getTarget();
                PropertyView property = (PropertyView) event.getData();
                propertyChanged(ProjectFileType.STEP_LIST, target.getOwner().getStepList(), property);
            }
        });
    }

    private void createOutputFileEventHandlers(TransformTable transformTable) {
        List<OutputFile> outputList = transformTable.getOutputList();
        for (OutputFile outputFile : outputList) {
            EventManager eventManager = outputFile.getEventManager();
            if (eventManager.countEventHandler(EventName.COLUMN_LIST_CHANGED) == 0) {
                log.debug("createOutputFileEventHandlers: addHandler on outputFile:{}", outputFile);
                eventManager.addHandler(EventName.COLUMN_LIST_CHANGED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        OutputFile target = (OutputFile) event.getTarget();
                        PropertyView property = (PropertyView) event.getData();
                        propertyChanged(target.getProjectFileType(), target, property);
                    }
                });
            } else {
                log.debug("createOutputFileEventHandlers: skip on outputFile:{}", outputFile);
            }
        }
    }

    public void submitZoom() {
        /*TODO: need to call StepSetting command to save settings to server*/

        String zoom = FacesUtil.getRequestParam("zoom").replaceAll("[%]", "").trim();
        if (zoom.isEmpty()) return;

        Step activeStep = workspace.getProject().getActiveStep();
        log.warn("zoom:{} step:{}", zoom, activeStep.getName());
        activeStep.setZoom(Double.valueOf(zoom));
    }

    public void requestAddStep() {
        Project project = workspace.getProject();

        Step step = addStep();
        if (step == null) {
            jsBuilder.pre(JavaScript.notiInfo, "Add step failed!");
            return;
        }

        refreshStepList(project.getStepList());
        selectStep(step.getIndex());

        createStepEventHandlers(step);

        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    private String createNewProject(int groupId) {
        return createNewProject(groupId, "");
    }

    private String createNewProject(int groupId, String templateId) {
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.WORKSPACE, workspace);
        paramMap.put(CommandParamKey.GROUP_ID, groupId);
        paramMap.put(CommandParamKey.TEMPLATE_ID, templateId);

        try {
            AddProject action = new AddProject(paramMap);
            action.execute();
            return (String) action.getResultMap().get(ActionResultKey.PROJECT_ID);
        } catch (Exception ex) {
            log.error("Create New Project Failed!" + ex.getMessage());
            log.trace("", ex);
            jsBuilder.pre(JavaScript.notiError, "Create New Project with Internal Command Error!");
            return null;
        }
    }

    private Step addStep() {
        Project project = workspace.getProject();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.PROJECT, project);

        AddStep action;
        try {
            action = new AddStep(paramMap);
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Add Step Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add Step Failed with Internal Command Error!");
            return null;
        }

        return (Step) action.getResultMap().get(ActionResultKey.STEP);
    }

    /**
     * TODO: need action for removeDataSourceSelector.
     */
    public void addDataSourceSelector() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        DataSourceSelector dataSourceSelector = new DataSourceSelector("Untitled", DataSourceType.LOCAL, ProjectUtil.newElementId(project));

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE_SELECTOR, dataSourceSelector);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataSourceSelector(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add DataSourceSelector Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add DataSourceSelector Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(dataSourceSelector.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    public void addLocal() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Local local = new Local("Untitled", "/");

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, local);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddLocal(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add File Directory Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add File Directory Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(local.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.post(JavaScript.refreshLocalList).runOnClient();
    }

    public void addDBConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Database database = new Database("Untitled", Dbms.ORACLE_SID);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, database);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataBase(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add Database Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add Database Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(database.getSelectableId());

        jsBuilder.post(JavaScript.refreshDatabaseList).runOnClient();
    }

    public void addSFTPConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        SFTP sftp = new SFTP("Untitled", "/");

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, sftp);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddSFTP(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add SFTP Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add SFTP Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(sftp.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.post(JavaScript.refreshSFTPList).runOnClient();
    }

    /**
     * TODO: need action for removeDataFile (UI: need to show 2 buttons on the endPlug, one for Remove-Line and one for Remove-Data-File).
     */
    public void addDataFile() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.STEP, step);

        Action action = new AddDataFile(paramMap);
        Selectable selectable = null;
        try {
            action.execute();
            selectable = (Selectable) action.getResultMap().get(ActionResultKey.DATA_FILE);
        } catch (RequiredParamException e) {
            log.error("Add DataFile Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add DataFile Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(selectable.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    public void undo(ActionView actionView) {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        List<Action> history = step.getHistory();
        Action action;
        int actionIndex = getActionIndex(actionView, actionList);
        if (actionIndex < 0) {
            String message = "Action '{" + actionView.getName() + "}' not found, Undo is aborted.";
            log.error(message);
            jsBuilder.pre(JavaScript.notiError, message);
            return;
        }

        for (int i = actionList.size() - 1; i >= actionIndex; i--) {
            ActionView view = actionList.remove(i);
            action = history.get(history.size() - 1);
            if (view.getId() != action.getId()) {
                String message = "Action List not match with the Action History Action(" + actionView + ") History(" + action + "), Undo is aborted.";
                log.error(message);
                jsBuilder.pre(JavaScript.notiError, message);
                return;
            }
            try {
                action.executeUndo();
            } catch (RequiredParamException e) {
                log.error("Undo '{}' Failed!", action.getName(), e);
                jsBuilder.pre(JavaScript.notiError, "Add " + action.getName() + " Failed with Internal Command Error!");
                return;
            }
        }

        refreshActionList(project);

        selectObject(step.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.post(JavaScript.notiInfo, "Undo[" + actionView.getName() + "] completed.");
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    private int getActionIndex(ActionView action, List<ActionView> actionViewList) {
        int i = 0;
        for (ActionView act : actionViewList) {
            if (act.getId() == action.getId()) return i;
            i++;
        }
        return -1;
    }

    /**
     * Set active object from client script in flowchart.
     */
    public void selectObject() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        log.warn("selectObject:fromClient(selectableId:'{}')", selectableId);
        selectObject(selectableId);
    }

    public void selectObject(String selectableId) {
        if (selectableId == null) {
            /*reset property sheet variables*/
            setPropertySheet(null);
            return;
        }

        Step step = workspace.getProject().getActiveStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Selectable activeObject = selectableMap.get(selectableId);
        if (activeObject == null) {
            setPropertySheet(null);
            return;
        }

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.SELECTABLE, activeObject);
        paramMap.put(CommandParamKey.STEP, step);
        try {
            new SelectObject(paramMap).execute();
        } catch (Exception e) {
            log.error("Select Object Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Select Object Failed with Internal Command Error!");
            return;
        }

        if (activeObject instanceof HasSelected) {
            ((HasSelected) activeObject).selected();
        }
        setPropertySheet(activeObject);

        /*OutputFile of TransformTable need some action*/
        if (activeObject instanceof TransformTable) {
            createOutputFileEventHandlers((TransformTable) activeObject);
        } else if (activeObject instanceof TransformColumn) {
            createOutputFileEventHandlers((TransformTable) ((TransformColumn) activeObject).getOwner());
        }
    }

    private void setPropertySheet(Selectable activeObject) {
        log.debug("setPropertySheet(selectable:{})", (activeObject != null ? activeObject.getSelectableId() : "null"));
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
        boolean fromClientMenu = Boolean.parseBoolean(FacesUtil.getRequestParam("fromMenu"));
        log.debug("setToolPanel:fromClient(fromMenu:{})", fromClientMenu);

        String refresh = FacesUtil.getRequestParam("refresh");
        if (refresh != null) {
            jsBuilder.pre(JavaScript.showStepList, showStepList, true)
                    .pre(JavaScript.showPropertyList, showPropertyList, true)
                    .pre(JavaScript.showActionButtons, showActionButtons, true)
                    .pre(JavaScript.showColumnNumbers, showColumnNumbers, true)
                    .runOnClient();
            log.debug(":checker(refresh)");
            return;
        }

        boolean propertyChanged = false;
        PropertyVar propertyVar = null;
        String stepList = FacesUtil.getRequestParam("stepList");
        if (stepList != null) {
            showStepList = Boolean.parseBoolean(stepList);
            step.setShowStepList(showStepList);
            propertyChanged = fromClientMenu;
            propertyVar = PropertyVar.showStepList;
            refreshStepList(project.getStepList());
            refreshActionList(project);
            log.debug(":checker(showStepList:{}, passedParameter:{})", showStepList, stepList);
        }

        String propertyList = FacesUtil.getRequestParam("propertyList");
        if (propertyList != null) {
            showPropertyList = Boolean.parseBoolean(propertyList);
            step.setShowPropertyList(showPropertyList);
            propertyChanged = fromClientMenu;
            propertyVar = PropertyVar.showPropertyList;
            log.debug(":checker(showPropertyList:{}, passedParameter:{})", showPropertyList, propertyList);
        }

        String actionButtons = FacesUtil.getRequestParam("actionButtons");
        if (actionButtons != null) {
            showActionButtons = Boolean.parseBoolean(actionButtons);
            step.setShowActionButtons(showActionButtons);
            propertyChanged = fromClientMenu;
            propertyVar = PropertyVar.showActionButtons;
            log.debug(":checker(showActionButtons:{}, passedParameter:{})", showActionButtons, actionButtons);
        }

        String columnNumbers = FacesUtil.getRequestParam("columnNumbers");
        if (columnNumbers != null) {
            showColumnNumbers = Boolean.parseBoolean(columnNumbers);
            step.setShowColumnNumbers(showColumnNumbers);
            propertyChanged = fromClientMenu;
            propertyVar = PropertyVar.showColumnNumbers;
            log.debug(":checker(showColumnNumbers:{}, passedParameter:{})", showColumnNumbers, columnNumbers);
        }

        if (propertyChanged) {
            PropertyView property = step.getProperties().getPropertyView(propertyVar.name());
            propertyChanged(ProjectFileType.STEP, step, property);
        }
    }

    public void stepListTabChanged(TabChangeEvent event) {
        String id = event.getTab().getId();
        log.warn("stepListTabChanged:fromClient(event:{}, tabId:{})", event, id);
        stepListActiveTab = 1;
        workspace.getProject().getActiveStep().setStepListActiveTab(stepListActiveTab);
    }

    public void refreshProperties() {
        setPropertySheet(workspace.getProject().getActiveStep().getActiveObject());
    }

    public String disabledClass(PropertyView property) {
        return disabled(property) ? " disabled" : "";
    }

    public boolean disabled(PropertyView property) {
        boolean disabled = false;
        boolean enabled = true;

        Properties properties = activeObject.getProperties();
        if (property.getDisableVar() != null) {
            disabled = (Boolean) properties.getPropertyValue(activeObject, property.getDisableVar(), log);
        }
        if (property.getEnableVar() != null) {
            enabled = (Boolean) properties.getPropertyValue(activeObject, property.getEnableVar(), log);
        }

        return disabled || !enabled;
    }

    public String masked(String value) {
        return new String(new char[value.length()]).replaceAll("\0", "*");
    }

    public Boolean isEmpty(Object object) {
        if (object == null) return true;
        if (object instanceof String) {
            String string = (String) object;
            if (string.contains(":")) {
                // itemList.DATASOURCE = "DataSourceType:dataSourceId"
                String[] parts = string.split("[:]");
                if (parts[0].isEmpty()) return true;
                return Integer.parseInt(parts[1]) == 0;
            } else {
                return string.isEmpty();
            }
        } else if (object instanceof Integer) {
            return ((Integer) object) == 0;
        }
        return false;
    }

    /**
     * for PropertyType.PROPERTIES
     */
    public void propertiesAppend() {
        String propertyVar = FacesUtil.getRequestParam("propertyVar");
        if (propertyVar == null || propertyVar.isEmpty()) {
            String msg = "propertiesAppend called without required propertyVar argument !";
            jsBuilder.pre(JavaScript.notiError, msg);
            log.error(msg);
            return;
        }

        boolean invalid = false;
        try {
            List<NameValue> nameValueList = (List<NameValue>) activeObject.getProperties().getPropertyValue(activeObject, propertyVar, log);
            String name, value;
            for (NameValue nameValue : nameValueList) {
                nameValue.setLast(false);
                name = nameValue.getName();
                value = nameValue.getValue();
                if (name == null || name.isEmpty() || value == null || value.isEmpty()) {
                    invalid = true;
                }
            }
            if (invalid) {
                nameValueList.get(nameValueList.size() - 1).setLast(true);
            } else {
                nameValueList.add(new NameValue(true));
            }
        } catch (Exception ex) {
            String msg = "propertiesAppend(propertyVar:" + propertyVar + ") found error";
            jsBuilder.pre(JavaScript.notiError, msg);
            log.error(msg + ex.getMessage());
            log.trace("", ex);
            return;
        }

        focusOnLastProperties = true;
        jsBuilder.pre(JavaScript.updateProperty, propertyVar)
                .post(JavaScript.focusProperty, 1000, propertyVar);

        if (invalid) {
            String msg = "Please correct empty value before append!";
            jsBuilder.pre(JavaScript.notiWarn, msg);
        }

        jsBuilder.runOnClient(true);
    }

    /**
     * for PropertyType.PROPERTIES
     */
    public void propertiesRemove(PropertyView property, int index) {
        try {
            List<NameValue> nameValueList = (List<NameValue>) activeObject.getProperties().getPropertyValue(activeObject, property.getVar(), log);
            nameValueList.remove(index);
        } catch (Exception ex) {
            String msg = "propertiesRemove(propertyVar:" + property.getVar() + ") found error";
            jsBuilder.pre(JavaScript.notiError, msg);
            log.error(msg + ex.getMessage());
            log.trace("", ex);
            return;
        }

        propertyChanged(property);

        focusOnLastProperties = true;
        jsBuilder.pre(JavaScript.updateProperty, property.getVar())
                .post(JavaScript.focusProperty, 1000, property.getVar())
                .runOnClient(true);
    }

    /**
     * for PropertyType.UPLOAD
     */
    public void uploadBinaryFile(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        FileUpload component = (FileUpload) event.getComponent();
        PropertyView property = activeObject.getProperties().getPropertyView(component.getStyleClass());

        /*turn uploadedFile to binaryFile*/
        BinaryFile binaryFile = new BinaryFile();
        binaryFile.setName(fileName);
        binaryFile.setContent(file.getContent());
        binaryFile.setExt(FileNameExtension.forName(fileName));
        log.debug("uploadBinaryFile: property: {}, uploadedFile: {}", property, binaryFile);

        boolean isVersionedFile = activeObject instanceof VersionedFile;
        if (!acceptFile(binaryFile, activeObject, property, isVersionedFile)) {
            /*notify message and log are in acceptFile function*/
            return;
        }

        /*send binaryFile to Action AddUploaded*/
        Action action;
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.WORKSPACE, workspace);
        paramMap.put(CommandParamKey.BINARY_FILE, binaryFile);
        paramMap.put(CommandParamKey.PROPERTY, property);
        paramMap.put(CommandParamKey.SELECTABLE, activeObject);
        try {
            if (isVersionedFile) {
                action = new AddVersioned(paramMap);
            } else {
                action = new AddUploaded(paramMap);
            }
            action.execute();
        } catch (Exception ex) {
            log.error("Uploaded File Failed! {}:{}", ex.getClass().getSimpleName(), ex.getMessage());
            log.trace("", ex);
            jsBuilder.pre(JavaScript.notiError, "Uploaded File Failed with Internal Command Error!");
            return;
        }

        List<Selectable> selectableList = (List) action.getResultMap().get(ActionResultKey.SELECTABLE_LIST);
        if (selectableList != null) {
            for (Selectable selectable : selectableList) {
                jsBuilder.pre(JavaScript.updateEm, selectable.getSelectableId());
            }
            jsBuilder.runOnClient();
        } else if (activeObject instanceof DataFile) {
            /*auto extract*/
            extractData(activeObject.getSelectableId());
        }

    }

    private boolean acceptFile(BinaryFile binaryFile, Selectable selectable, PropertyView property, boolean isVersionedFile) {

        // Notice: OWASP: need mime-type from Apache-Tika, don't use file.getContentType() it always return 'application/octet-stream'.
        String allowMimeTypes = isVersionedFile ? ((VersionedFile) activeObject).getId().getAllowMimeType() : "text/plain";
        Tika tika = new Tika();
        String mimeType = tika.detect(binaryFile.getContent());
        log.debug("mimetype:{}, allowMimeTypes:{}", mimeType, allowMimeTypes);
        if (!Pattern.compile(allowMimeTypes).matcher(mimeType).find()) {
            jsBuilder.pre(JavaScript.notiWarn, property.getParams()[2]);
            return false;
        }

        /*TODO: need configs: max.uploaded.bytes and max.versioned.bytes */
        EnvironmentConfigs configs = EnvironmentConfigs.valueOf(workspace.getEnvironment().name());

        return true;
    }

    /**
     * for PropertyType.COLUMNLIST
     */
    public void initColumnList(PropertyView property) {
        /*Notice: used in OutputFile only*/
        OutputFile outputFile = (OutputFile) this.activeObject;

        List<String> columnList = new ArrayList<>();
        DataTable sourceTable = (DataTable) outputFile.getOwner();
        for (DataColumn column : sourceTable.getColumnList()) {
            columnList.add(column.getName());
        }

        try {
            property.setNewValue(columnList);
            activeObject.getProperties().setPropertyValue(activeObject, property, log);
        } catch (Exception ex) {
            String msg = "Init ColumnList Property Failed! {}";
            jsBuilder.pre(JavaScript.notiError, msg, ex.getMessage());
            log.error(msg, ex.getMessage());
            log.trace(FacesUtil.getFormattedStackTrace(ex, "com.tflow", "\n"));
            return;
        }

        propertyChanged(activeObject.getProjectFileType(), activeObject, property);
    }

    /**
     * for PropertyType.COLUMNLIST
     */
    public void columnListSelect(SelectEvent<String> event) {
        /*Cancel added item by remove it from the list*/

        Properties properties = activeObject.getProperties();
        Chips chips = (Chips) event.getComponent();
        PropertyView property = properties.getPropertyView(chips.getStyleClass());
        String item = event.getObject();

        try {
            List<String> columnList = (List<String>) properties.getPropertyValue(activeObject, property, log);
            columnList.remove(item);
        } catch (Exception ex) {
            String msg = "{}";
            jsBuilder.pre(JavaScript.notiError, msg, ex.getMessage());
            log.error(msg, ex.getMessage());
            log.trace(FacesUtil.getFormattedStackTrace(ex, "com.tflow", "\n"));
        }
    }

    /**
     * for PropertyType.COLUMNLIST
     */
    public void columnListUnselect(UnselectEvent<String> event) {
        Properties properties = activeObject.getProperties();
        Chips chips = (Chips) event.getComponent();
        PropertyView property = properties.getPropertyView(chips.getStyleClass());
        propertyChanged(activeObject.getProjectFileType(), activeObject, property);
    }

    public void testDatabaseConnection(PropertyView property) {
        Database database = (Database) activeObject;
        int dataSourceId = database.getId();
        if (!isDatabaseReady(dataSourceId)) {
            jsBuilder.pre(JavaScript.notiWarn, "Connection fail!");
            return;
        }
        jsBuilder.pre(JavaScript.notiInfo, "Connect successful!");
    }

    public void testSFTPConnection(PropertyView property) {
        /*test sftp connection using DConvers*/
        DConversHelper dconvers = new DConversHelper(true);
        SFTP sftp = (SFTP) activeObject;
        int sftpId = sftp.getId();
        dconvers.addSFTP(sftpId, workspace.getProject());
        if (!dconvers.run()) {
            jsBuilder.pre(JavaScript.notiWarn, "Connection fail!");
            return;
        }

        jsBuilder.pre(JavaScript.notiInfo, "Connect successful!");
    }
}
