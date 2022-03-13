package com.tflow.model.editor;

import com.tflow.HasEvent;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.Tower;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step implements Serializable, Selectable, HasEvent {
    private static final long serialVersionUID = 2021121709996660002L;

    private int id;
    private String name;
    private int index;
    private List<Action> history;
    private List<DataTable> dataList;
    private List<TransformTable> transformList;
    private List<DataFile> outputList;

    private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;

    private List<Line> lineList;
    private int lastLineClientIndex;

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

    public Step(String name, Project owner) {
        this.name = name;
        history = new ArrayList<>();
        dataList = new ArrayList<>();
        transformList = new ArrayList<>();
        outputList = new ArrayList<>();
        dataTower = new Tower(3, this);
        transformTower = new Tower(2, this);
        outputTower = new Tower(2, this);
        lineList = new ArrayList<>();
        lastLineClientIndex = 0;
        startPlug = new StartPlug("step");
        zoom = Double.valueOf(100);
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

    public Project getOwner() {
        return owner;
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

    /**
     * Internal use to add new line to the step without history.
     */
    public Line addLine(String startSelectableId, String endSelectableId) {
        Line newLine = new Line(startSelectableId, endSelectableId);

        newLine.setClientIndex(newLineClientIndex());
        lineList.add(newLine);

        Selectable startSelectable = selectableMap.get(startSelectableId);
        Selectable endSelectable = selectableMap.get(endSelectableId);

        newLine.setType(getLineType(startSelectable));

        LinePlug startPlug = startSelectable.getStartPlug();
        startPlug.setPlugged(true);
        startPlug.getLineList().add(newLine);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setStartPlug(startPlug);

        HasEndPlug hasEndPlug = (HasEndPlug) endSelectable;
        LinePlug endPlug = hasEndPlug.getEndPlug();
        endPlug.setPlugged(true);
        endPlug.getLineList().add(newLine);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setEndPlug(endPlug);

        return newLine;
    }

    /**
     * Internal use to remove line from the step without history.
     */
    public void removeLine(Line line) {
        lineList.remove(line);

        LinePlug startPlug = line.getStartPlug();
        startPlug.getLineList().remove(line);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }

        LinePlug endPlug = line.getEndPlug();
        endPlug.getLineList().remove(line);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }
    }

    /**
     * Internal use to remove line from the step without history.
     */
    public void removeLine(LinePlug plug) {
        List<Line> lineList = new ArrayList<>(plug.getLineList());
        if (lineList.size() > 0) {
            for (Line line : lineList) {
                removeLine(line);
            }
        }
    }

    public LineType getLineType(Selectable selectable) {
        if (selectable instanceof DataColumn) {
            DataColumn dataColumn = (DataColumn) selectable;
            return LineType.valueOf(dataColumn.getType().name());
        } else if (selectable instanceof ColumnFx) {
            ColumnFx columnFx = (ColumnFx) selectable;
            return LineType.valueOf(columnFx.getOwner().getType().name());
        } else {
            return LineType.TABLE;
        }
    }

    public List<Line> getLineByStart(String selectableId) {
        List<Line> found = new ArrayList<>();
        for (Line line : lineList) {
            if (line.getStartSelectableId().equals(selectableId)) {
                found.add(line);
            }
        }
        return found;
    }

    public List<Line> getLineByEnd(String selectableId) {
        List<Line> found = new ArrayList<>();
        for (Line line : lineList) {
            if (line.getEndSelectableId().equals(selectableId)) {
                found.add(line);
            }
        }
        return found;
    }

    public void refresh() {
        collectSelectableToMap();
        assignLineIndexes();
    }

    private void assignLineIndexes() {
        int clientIndex = 0;
        for (Line line : lineList) {
            line.setClientIndex(clientIndex++);
        }
    }

    private void collectSelectableToMap() {
        selectableMap = new HashMap<>();
        selectableMap.put(this.getSelectableId(), this);

        if (getActiveObject() == null) {
            setActiveObject(this);
        }

        List<Selectable> selectableList = dataTower.getSelectableList();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = transformTower.getSelectableList();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = outputTower.getSelectableList();
        collectSelectableTo(selectableMap, selectableList);
    }

    /**
     * IMPORTANT: when selectable object is added, need to add script to collect them in this function.
     */
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
                    for (ColumnFx columnFx : tt.getColumnFxTable().getColumnFxList()) {
                        map.put(columnFx.getSelectableId(), columnFx);

                        for (ColumnFxPlug columnFxPlug : columnFx.getEndPlugList()) {
                            map.put(columnFxPlug.getSelectableId(), columnFxPlug);
                        }
                    }

                    for (TableFx tableFx : tt.getFxList()) {
                        map.put(tableFx.getSelectableId(), tableFx);
                    }
                }

            }
        }
    }

    public int newLineClientIndex() {
        return ++lastLineClientIndex;
    }


    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", index:" + index +
                ", name:'" + name + '\'' +
                ", zoom:" + zoom +
                ", showStepList:" + showStepList +
                ", showPropertyList:" + showPropertyList +
                ", showActionButtons:" + showActionButtons +
                '}';
    }
}
