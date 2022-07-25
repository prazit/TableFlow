package com.tflow.model.editor;

import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.DataTableUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step implements Selectable, HasEvent {

    private int id;
    private String name;

    /* Notice: index >= 0 = Working Data, index < 0 = Label Data */
    private int index;

    private List<Action> history;
    private List<DataSourceSelector> dataSourceSelectorList;

    private List<DataFile> fileList;
    private List<DataTable> dataList;
    private List<TransformTable> transformList;
    private List<DataFile> outputList;

    private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;

    private List<Line> lineList;
    private int lastLineClientIndex;

    /* TODO: future featured: show all steps in one flowchart*/
    private LinePlug startPlug;

    private Project owner;

    private Selectable activeObject;
    private Double zoom;
    private boolean showStepList;
    private boolean showPropertyList;
    private boolean showActionButtons;
    private int stepListActiveTab;

    private Map<String, Selectable> selectableMap;

    private EventManager eventManager;

    /* for StepMapper */
    public Step() {
        /*nothting*/
        init("", null);
    }

    public Step(String name, Project owner) {
        init(name, owner);
        this.id = DataTableUtil.newUniqueId(owner);
        dataTower = new Tower(DataTableUtil.newUniqueId(owner), 3, this);
        transformTower = new Tower(DataTableUtil.newUniqueId(owner), 2, this);
        outputTower = new Tower(DataTableUtil.newUniqueId(owner), 2, this);
    }

    private void init(String name, Project owner) {
        this.name = name;
        history = new ArrayList<>();
        dataSourceSelectorList = new ArrayList<>();
        fileList = new ArrayList<>();
        dataList = new ArrayList<>();
        transformList = new ArrayList<>();
        outputList = new ArrayList<>();
        lineList = new ArrayList<>();
        lastLineClientIndex = 0;
        startPlug = new StartPlug("step");
        zoom = 100d;
        this.owner = owner;
        selectableMap = new HashMap<>();
        showStepList = true;
        showPropertyList = true;
        showActionButtons = true;
        eventManager = new EventManager(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Action> getHistory() {
        return history;
    }

    public void setHistory(List<Action> history) {
        this.history = history;
    }

    public List<DataSourceSelector> getDataSourceSelectorList() {
        return dataSourceSelectorList;
    }

    public void setDataSourceSelectorList(List<DataSourceSelector> dataSourceSelectorList) {
        this.dataSourceSelectorList = dataSourceSelectorList;
    }

    public List<DataFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<DataFile> fileList) {
        this.fileList = fileList;
    }

    public DataFile getFile(int id) {
        for (DataFile dataFile : fileList) {
            if (id == dataFile.getId()) {
                return dataFile;
            }
        }
        return null;
    }

    public List<DataTable> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataTable> dataList) {
        this.dataList = dataList;
    }

    public List<TransformTable> getTransformList() {
        return transformList;
    }

    public void setTransformList(List<TransformTable> transformList) {
        this.transformList = transformList;
    }

    public List<DataFile> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<DataFile> outputList) {
        this.outputList = outputList;
    }

    public Tower getDataTower() {
        return dataTower;
    }

    public void setDataTower(Tower dataTower) {
        this.dataTower = dataTower;
    }

    public Tower getTransformTower() {
        return transformTower;
    }

    public void setTransformTower(Tower transformTower) {
        this.transformTower = transformTower;
    }

    public Tower getOutputTower() {
        return outputTower;
    }

    public void setOutputTower(Tower outputTower) {
        this.outputTower = outputTower;
    }

    /**
     * Notice: Step.LineList is Read only, don't make any change to it.
     */
    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public Project getOwner() {
        return owner;
    }

    public void setOwner(Project owner) {
        this.owner = owner;
    }

    public DataTable getDataTable(int sourceId) {
        for (DataTable dataTable : dataList) {
            if (dataTable.getId() == sourceId) {
                return dataTable;
            }
        }
        return null;
    }

    public TransformTable getTransformTable(int sourceId) {
        for (TransformTable transformTable : transformList) {
            if (transformTable.getId() == sourceId) {
                return transformTable;
            }
        }
        return null;
    }

    public Selectable getActiveObject() {
        return activeObject;
    }

    public void setActiveObject(Selectable activeObject) {
        this.activeObject = activeObject;
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
    }

    public Double getZoom() {
        return zoom;
    }

    public Map<String, Selectable> getSelectableMap() {
        return selectableMap;
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

    public int getStepListActiveTab() {
        return stepListActiveTab;
    }

    public void setStepListActiveTab(int stepListActiveTab) {
        this.stepListActiveTab = stepListActiveTab;
    }

    @Override
    public Properties getProperties() {
        return Properties.STEP;
    }

    @Override
    public String getSelectableId() {
        return "step" + id;
    }

    @Override
    public LinePlug getStartPlug() {
        return startPlug;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    /*== Public Methods ==*/

    public int getLastLineClientIndex() {
        return lastLineClientIndex;
    }

    public void setLastLineClientIndex(int lastLineClientIndex) {
        this.lastLineClientIndex = lastLineClientIndex;
    }


    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", index:" + index +
                ", history:" + history +
                ", dataSourceSelectorList:" + dataSourceSelectorList +
                ", fileList:" + fileList +
                ", dataList:" + dataList +
                ", transformList:" + transformList +
                ", outputList:" + outputList +
                ", dataTower:" + dataTower +
                ", transformTower:" + transformTower +
                ", outputTower:" + outputTower +
                //", lineList:" + lineList +
                ", lastLineClientIndex:" + lastLineClientIndex +
                ", startPlug:" + startPlug.getPlug() +
                ", activeObject:" + ((activeObject == null) ? "null" : activeObject.getSelectableId()) +
                ", zoom:" + zoom +
                ", showStepList:" + showStepList +
                ", showPropertyList:" + showPropertyList +
                ", showActionButtons:" + showActionButtons +
                ", stepListActiveTab:" + stepListActiveTab +
                '}';
    }
}
