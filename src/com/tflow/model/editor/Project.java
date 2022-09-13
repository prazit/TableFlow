package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectType;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project implements Selectable, HasEvent {
    private int groupId;
    private String id;
    private String name;
    private ProjectType type;
    private int activeStepIndex;

    private List<Step> stepList;

    private Map<Integer, Database> databaseMap;
    private Map<Integer, SFTP> sftpMap;
    private Map<Integer, Local> localMap;

    private Map<String, Variable> variableMap;

    private int lastElementId;
    private int lastUniqueId;

    private Map<String, Object> propertyMap;

    private transient Workspace owner;

    private transient ProjectMapper mapper;
    private transient ProjectManager manager;
    private transient DataManager dataManager;
    private transient EventManager eventManager;

    /*for ProjectMapper*/
    public Project() {
        init();
    }

    public Project(String id, String name) {
        this.id = id;
        activeStepIndex = -1;
        this.name = name;
        databaseMap = new HashMap<>();
        sftpMap = new HashMap<>();
        localMap = new HashMap<>();
        variableMap = new HashMap<>();
        init();
    }

    private void init() {
        stepList = new ArrayList<>();
        propertyMap = new HashMap<>();
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.name.equals(property.getVar())) {
                    eventManager.fireEvent(EventName.NAME_CHANGED, property);
                }
            }
        });
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.PROJECT;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectType getType() {
        return type;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    public int getActiveStepIndex() {
        return activeStepIndex;
    }

    public void setActiveStepIndex(int activeStepIndex) {
        this.activeStepIndex = activeStepIndex;
    }

    public List<Step> getStepList() {
        return stepList;
    }

    public void setStepList(List<Step> stepList) {
        this.stepList = stepList;
    }

    public Map<Integer, Database> getDatabaseMap() {
        return databaseMap;
    }

    public void setDatabaseMap(Map<Integer, Database> databaseMap) {
        this.databaseMap = databaseMap;
    }

    public Map<Integer, SFTP> getSftpMap() {
        return sftpMap;
    }

    public void setSftpMap(Map<Integer, SFTP> sftpMap) {
        this.sftpMap = sftpMap;
    }

    public Map<Integer, Local> getLocalMap() {
        return localMap;
    }

    public void setLocalMap(Map<Integer, Local> localMap) {
        this.localMap = localMap;
    }

    public Map<String, Variable> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(Map<String, Variable> variableMap) {
        this.variableMap = variableMap;
    }

    public Workspace getOwner() {
        return owner;
    }

    public void setOwner(Workspace workspace) {
        this.owner = workspace;
    }

    public ProjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ProjectMapper mapper) {
        this.mapper = mapper;
    }

    public ProjectManager getManager() {
        return manager;
    }

    public void setManager(ProjectManager manager) {
        this.manager = manager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public int getLastElementId() {
        return lastElementId;
    }

    public void setLastElementId(int lastElementId) {
        this.lastElementId = lastElementId;
    }

    public int getLastUniqueId() {
        return lastUniqueId;
    }

    public void setLastUniqueId(int lastUniqueId) {
        this.lastUniqueId = lastUniqueId;
    }

    @Override
    public String getSelectableId() {
        return id;
    }

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Properties getProperties() {
        return Properties.PROJECT;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }


    public Step getActiveStep() {
        if (activeStepIndex < 0 || activeStepIndex >= stepList.size()) return null;
        return stepList.get(activeStepIndex);
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String toString() {
        return "{" +
                "groupId:'" + groupId + '\'' +
                ", projectId:'" + id + '\'' +
                ", name:'" + name + '\'' +
                ", activeStepIndex:" + activeStepIndex +
                ", stepList:" + stepList +
                ", databaseMap:" + databaseMap +
                ", sftpMap:" + sftpMap +
                ", localMap:" + localMap +
                ", variableMap:" + variableMap +
                ", lastElementId:" + lastElementId +
                ", lastUniqueId:" + lastUniqueId +
                '}';
    }

}
