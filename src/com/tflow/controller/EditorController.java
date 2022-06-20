package com.tflow.controller;

import com.tflow.HasEvent;
import com.tflow.kafka.KafkaRecordValue;
import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.view.ActionView;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.system.constant.Theme;
import com.tflow.util.FacesUtil;
import com.tflow.util.SerializeUtil;
import com.tflow.wcmd.TWcmd;
import net.mcmanus.eamonn.serialysis.SEntity;
import net.mcmanus.eamonn.serialysis.SerialScan;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.primefaces.event.TabChangeEvent;
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
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.*;

@ViewScoped
@Named("editorCtl")
public class EditorController extends Controller {

    @Inject
    private Workspace workspace;

    private String projectName;
    private MenuModel stepMenu;
    private Double zoom;

    private List<ActionView> actionList;

    private List<PropertyView> propertyList;
    private Selectable activeObject;

    private String leftPanelTitle;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private int stepListActiveTab;

    private Map<String, Integer> actionPriorityMap;
    private boolean fullActionList;

    @PostConstruct
    public void onCreation() {
        Project project = workspace.getProject();
        leftPanelTitle = "Step List";
        initActionPriorityMap();
        initStepList(project);
        //refreshActionList(project);
    }

    private void initActionPriorityMap() {
        actionPriorityMap = new HashMap<>();
        actionPriorityMap.put("RML", 1);
        actionPriorityMap.put("AML", 2);
    }

    private void initStepList(Project project) {
        projectName = project.getName();
        refreshStepList(project.getStepList());
        selectStep(project.getActiveStepIndex(), false);
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
                DataTable activeDataTable = getActiveDataTable(activeObject);
                if (activeDataTable == null) break;

                int level = activeDataTable.getLevel();
                for (DataTable dataTable : activeStep.getDataList()) {
                    if (dataTable.getLevel() >= level) /*list all tables before current table only*/ continue;
                    selectItemList.add(new SelectItem(dataTable.getSelectableId(), dataTable.getName()));
                }
                break;

            case COLUMN:
                /* params[0] is property-name that contains selectable-id of source-table*/
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

    private DataTable getActiveDataTable(Selectable activeObject) {
        /*need to support SOURCETABLE on ColumnFx, DataColumn, TableFx and DataFile(output)*/
        if (activeObject instanceof DataTable) return (DataTable) activeObject;
        if (activeObject instanceof ColumnFx) return ((ColumnFx) activeObject).getOwner().getOwner();
        if (activeObject instanceof DataColumn) return ((DataColumn) activeObject).getOwner();
        if (activeObject instanceof TableFx) return ((TableFx) activeObject).getOwner();
        if (activeObject instanceof DataFile) return (DataTable) ((DataFile) activeObject).getOwner();
        return null;
    }

    private String propertyToMethod(String propertyName) {
        return "get" +
                propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
    }

    private Object getPropertyValue(Selectable selectable, String propertyName) {
        Object value = selectable.getPropertyMap().get(propertyName);
        if (value != null) return value;

        try {
            /*by getValue() method*/
            value = selectable.getClass().getMethod(propertyToMethod(propertyName)).invoke(selectable);
        } catch (Exception e) {
            /*by property.var*/
            value = getPropertyValue(selectable, selectable.getProperties().getPropertyView(propertyName));
        }

        return value == null ? "" : value;
    }

    private Object getPropertyValue(Selectable selectable, PropertyView property) {
        Object value = null;

        if (property.hasParent())
            /*by getParent().getValue() method, the parent always be the PropertyMap*/
            value = selectable.getPropertyMap().get(projectName);
        else
            try {
                /*by getValue() method without parent*/
                value = selectable.getClass().getMethod(propertyToMethod(property.getVar())).invoke(selectable);
            } catch (Exception e) {
                /*no property*/
                log.warn("getPropertyValue: no compatible method to get value from property({})", property);
                log.error("this is debug information", e);
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

    private int messageNo;

    private Producer<String, String> producer;
    private long producerLastClose = 0;

    public void testKafkaSendMessage() {
        messageNo++;

        /* using Kafka lib -- https://www.tutorialspoint.com/apache_kafka/apache_kafka_simple_producer_example.htm */
        String topic = "quickstart-events";
        String key = "Key#" + messageNo;
        String value = "";

        List<Action> history = workspace.getProject().getActiveStep().getHistory();
        KafkaTWAdditional additional = new KafkaTWAdditional();
        additional.setProjectId(workspace.getProject().getName());
        additional.setModifiedClientId(3);
        additional.setModifiedUserId(23);
        try {
            KafkaRecordValue kafkaRecordValue = new KafkaRecordValue(SerializeUtil.serialize(history), additional);
            value = SerializeUtil.serialize(kafkaRecordValue);

            kafkaRecordValue = (KafkaRecordValue) SerializeUtil.deserialize(value);
            kafkaRecordValue.setData(SerializeUtil.deserialize((String) kafkaRecordValue.getData()));

            log.warn("testSendMessage: deserialize kafkaRecordValue: {}", kafkaRecordValue);
        } catch (IOException | ClassNotFoundException ex) {
            log.error("testSendMessage: ", ex);
        }

        if (producer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "DESKTOP-K1PAMA3:9092");
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("key.serializer.encoding", "UTF-8");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer.encoding", "UTF-8");
            producer = new KafkaProducer<String, String>(props);
        }

        /* need to know server connection status before send the message */
        try {
            Metric metric;
            MetricName metricName;
            long closeCount = 0;
            long creationCount = -1;
            for (Map.Entry<MetricName, ? extends Metric> mapEntry : producer.metrics().entrySet()) {
                metric = mapEntry.getValue();
                metricName = metric.metricName();
                if (metricName.name().compareTo("connection-close-total") == 0) {
                    closeCount = ((Double) metric.metricValue()).longValue();
                } else if (metricName.name().compareTo("connection-creation-total") == 0) {
                    creationCount = ((Double) metric.metricValue()).longValue();
                }
            }

            log.warn("testSendMessage: connection-close-total = {}, connection-creation-total = {}", closeCount, creationCount);
            if (closeCount > producerLastClose || creationCount == 0) {
                producerLastClose = closeCount;
                log.error("testSendMessage: Kafka is down! the message is not sent, key:{}, value:{}", key, value);
                return;
            }

        } catch (NullPointerException ex) {
            log.error("testSendMessage: Kafka metric not valid! the message is not sent, key:{}, value:{}", key, value);
            return;
        }

        /*String value = "MessageValue#" + messageNo;*/
        producer.send(new ProducerRecord<String, String>(topic, key, value));
        log.warn("testSendMessage(key:{}, value:{}) completed.", key, value);

        /*TODO: remove test line below*/
        testWriteSerialize(value, null, null, "/Apps/TFlow/TestSerializeKafka.ser");
    }

    private void testConvertByteArrayAndString(KafkaRecordValue kafkaRecordValue) {
        /*#1 using ByteStream*/
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(kafkaRecordValue);
            objectOutputStream.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            KafkaRecordValue deserilizedKafkaRecordValue = (KafkaRecordValue) objectInputStream.readObject();
            objectInputStream.close();
            log.warn("testConvertByteArrayAndString: #1 serilizedKafkaRecordValue: {}", Arrays.toString(byteArrayOutputStream.toByteArray()));
            log.warn("testConvertByteArrayAndString: #1 deserilizedKafkaRecordValue: {}", deserilizedKafkaRecordValue);

            String value = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
            byteArrayInputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.ISO_8859_1));
            log.warn("testConvertByteArrayAndString: #2 serilizedKafkaRecordValue: {}", Arrays.toString(value.getBytes()));
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            deserilizedKafkaRecordValue = (KafkaRecordValue) objectInputStream.readObject();
            objectInputStream.close();
            log.warn("testConvertByteArrayAndString: #2 deserilizedKafkaRecordValue: {}", deserilizedKafkaRecordValue);

        } catch (Exception ex) {
            log.error("testConvertByteArrayAndString: ", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void testReadActionList() {
        /*Notice: serialized file can contains Footer-Data after serialized-data without any concern.*/
        /*Notice: serialized file can contains Header-Data before serialized-data when use SerialScan.readObject to read.*/
        /*Notice: IMPORTANT: serialized file can't contains Header-Data before serialized-data when use Object.readObject to read.*/
        List<Action> actionList = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/TestSerialize.ser");

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
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/TestSerialize.ser");

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
            log.error("testReadFromFile: ", ex);
        }

        return value;
    }

    public void testReadKafkaRecordValue() {
        KafkaRecordValue kafkaRecordValue = null;
        try {
            FileInputStream fileIn = new FileInputStream("/Apps/TFlow/TestSerialize.ser");

            /*-- normal cast to known object --*/
            ObjectInputStream in = new ObjectInputStream(fileIn);
            kafkaRecordValue = (KafkaRecordValue) in.readObject();
            in.close();

            fileIn.close();
        } catch (IOException i) {
            log.error("", i);
        } catch (ClassNotFoundException c) {
            log.error("List<Action> class not found", c);
        }

        if (kafkaRecordValue == null) {
            log.error("KafkaRecordValue is Null");
            return;
        }

        log.info("kafkaRecordValue = {}", kafkaRecordValue.toString());
    }

    public void testScanSerialize() {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream("/Apps/TFlow/TestSerialize.ser");

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
        KafkaTWAdditional additional = new KafkaTWAdditional();
        additional.setProjectId(workspace.getProject().getName());
        additional.setModifiedClientId(3);
        additional.setModifiedUserId(23);
        KafkaRecordValue kafkaRecordValue = new KafkaRecordValue(history, additional);
        testWriteSerialize(kafkaRecordValue, null, null);
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
                fileName = "/Apps/TFlow/TestSerialize.ser";
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
            log.info("Serialized data is saved in /Apps/TFlow/TestSerialize.ser");
        } catch (IOException i) {
            log.error("testWriteSerialize failed,", i);
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
        showActionButtons = activeStep.isShowActionButtons();
        stepListActiveTab = activeStep.getStepListActiveTab();

        Selectable activeObject = activeStep.getActiveObject();
        if (activeObject == null) {
            selectObject(null);
        } else {
            selectObject(activeObject.getSelectableId());
        }

        refreshActionList(project);

        StringBuilder jsBuilder = new StringBuilder();
        if (refresh) {
            jsBuilder.append(JavaScript.refreshFlowChart.getScript());
        }
        FacesUtil.runClientScript(jsBuilder.toString());
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
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    public void addLocal() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Local local = new Local("Untitled", "/", project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, local);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddLocal(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add File Directory Failed!", e);
            FacesUtil.addError("Add File Directory Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(local.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.addInfo("Local[" + local.getName() + "] added.");
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    public void addDBConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        Database database = new Database("Untitled", Dbms.ORACLE, project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, database);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddDataBase(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add Database Failed!", e);
            FacesUtil.addError("Add Database Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(database.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.addInfo("Database[" + database.getName() + "] added.");
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

    public void addSFTPConnection() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        SFTP sftp = new SFTP("Untitled", "/", project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_SOURCE, sftp);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            new AddSFTP(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add SFTP Failed!", e);
            FacesUtil.addError("Add SFTP Failed with Internal Command Error!");
            return;
        }

        refreshActionList(project);

        selectObject(sftp.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.addInfo("SFTP[" + sftp.getName() + "] added.");
        FacesUtil.runClientScript(JavaScript.refreshFlowChart.getScript());
    }

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
            FacesUtil.addError("Add DataFile Failed with Internal Command Error!");
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
            FacesUtil.addError(message);
            return;
        }

        for (int i = actionList.size() - 1; i >= actionIndex; i--) {
            ActionView view = actionList.remove(i);
            action = history.get(history.size() - 1);
            if (view.getId() != action.getId()) {
                String message = "Action List not match with the Action History Action(" + actionView + ") History(" + action + "), Undo is aborted.";
                log.error(message);
                FacesUtil.addError(message);
                return;
            }
            try {
                action.executeUndo();
            } catch (RequiredParamException e) {
                log.error("Undo '{}' Failed!", action.getName(), e);
                FacesUtil.addError("Add " + action.getName() + " Failed with Internal Command Error!");
                return;
            }
        }

        refreshActionList(project);

        selectObject(step.getSelectableId());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.addInfo("Undo[" + actionView.getName() + "] completed.");
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

        step.setActiveObject(activeObject);
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
                    + "showPropertyList(" + showPropertyList + ");"
                    + "showActionButtons(" + showActionButtons + ");";
            FacesUtil.runClientScript(javascript);
        }

        String stepList = FacesUtil.getRequestParam("stepList");
        if (stepList != null) {
            showStepList = Boolean.parseBoolean(stepList);
            step.setShowStepList(showStepList);
            refreshStepList(project.getStepList());
            refreshActionList(project);
        }

        String propertyList = FacesUtil.getRequestParam("propertyList");
        if (propertyList != null) {
            showPropertyList = Boolean.parseBoolean(propertyList);
            step.setShowPropertyList(showPropertyList);
        }

        String actionButtons = FacesUtil.getRequestParam("actionButtons");
        if (actionButtons != null) {
            showActionButtons = Boolean.parseBoolean(actionButtons);
            step.setShowActionButtons(showActionButtons);
            log.warn("setToolPanel(showActionButtons:{}, passedParameter:{})", showActionButtons, actionButtons);
        }
    }

    public void stepListTabChanged(TabChangeEvent event) {
        String id = event.getTab().getId();
        log.warn("stepListTabChanged(event:{}, tabId:{})", event, id);
        int activeTabIndex = 1;
        stepListActiveTab = activeTabIndex;
        workspace.getProject().getActiveStep().setStepListActiveTab(stepListActiveTab);
    }

    public void propertyChanged(PropertyView property) {
        Selectable activeObject = workspace.getProject().getActiveStep().getActiveObject();
        Object value = getPropertyValue(activeObject, property);
        log.warn("propertyChanged(property:{}, value:{})", property, value);

        if (activeObject instanceof HasEvent) {
            HasEvent hasEvent = (HasEvent) activeObject;
            hasEvent.getEventManager().fireEvent(EventName.PROPERTY_CHANGED, property);
        }
    }

}
