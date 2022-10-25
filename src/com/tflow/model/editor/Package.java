package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PackageType;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.view.PropertyView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Package implements Selectable, HasEvent {

    private boolean lock;
    private int id;
    private PackageType type;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;
    private boolean finished;

    private List<PackageFile> fileList;
    private int lastFileId;

    private EventManager eventManager;

    public Package() {
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
        return ProjectFileType.PACKAGE;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PackageType getType() {
        return type;
    }

    public void setType(PackageType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }

    public Date getBuiltDate() {
        return builtDate;
    }

    public void setBuiltDate(Date builtDate) {
        this.builtDate = builtDate;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<PackageFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<PackageFile> fileList) {
        this.fileList = fileList;
    }

    public int getLastFileId() {
        return lastFileId;
    }

    public void setLastFileId(int lastFileId) {
        this.lastFileId = lastFileId;
    }

    public boolean isNameDisabled() {
        return lock || complete < 100 || !finished;
    }

    @Override
    public String toString() {
        return "{" +
                ", lock:" + lock +
                ", id:" + id +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", buildDate:" + buildDate +
                ", builtDate:" + builtDate +
                ", complete:" + complete +
                ", finished:" + finished +
                ", lastFileId:" + lastFileId +
                ", fileList:" + (fileList == null ? 0 : fileList.size()) +
                '}';
    }

    @Override
    public Properties getProperties() {
        return Properties.PACKAGE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.PACKAGE.getPrefix() + id;
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
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }
}
