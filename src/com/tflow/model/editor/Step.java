package com.tflow.model.editor;

import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.Tower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step {

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

    private Project owner;

    private Selectable activeObject;
    private Double zoom;

    private Map<String, Selectable> selectableMap;

    public Step(int id, String name, int index, Project owner) {
        this.id = id;
        this.name = name;
        this.index = index;
        history = new ArrayList<>();
        dataList = new ArrayList<>();
        transformList = new ArrayList<>();
        outputList = new ArrayList<>();
        dataTower = new Tower(3, this);
        transformTower = new Tower(2, this);
        outputTower = new Tower(2, this);
        lineList = new ArrayList<>();
        zoom = Double.valueOf(100);
        this.owner = owner;
        selectableMap = new HashMap<>();
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
     * If you need to add line to the list must call addLine().
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

    public void setSelectableMap(Map<String, Selectable> selectableMap) {
        this.selectableMap = selectableMap;
    }

    /*== Public Methods ==*/

    public void addLine(String startSelectableId, String endSelectableId) {
        Line newLine = new Line(startSelectableId, endSelectableId);

        int index = lineList.size();
        newLine.setClientIndex(index);

        Selectable startSelectable = selectableMap.get(startSelectableId);
        newLine.setType(getLineType(startSelectable));
        newLine.setStartPlug(startSelectable.getStartPlug());

        HasEndPlug hasEndPlug = (HasEndPlug) selectableMap.get(endSelectableId);
        if (hasEndPlug == null) {
            LoggerFactory.getLogger(Step.class).error("selectableMap not contains selectableId={}", endSelectableId);
            return;
        }
        newLine.setEndPlug(hasEndPlug.getEndPlug());

        lineList.add(newLine);
    }

    private LineType getLineType(Selectable selectable) {
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
            if (line.getEndSelectableId().equals(selectableId)) found.add(line);
        }
        return found;
    }

    public void collectSelectableToMap() {
        List<Selectable> selectableList = dataTower.getSelectableList();
        Selectable activeObject = getActiveObject();
        if (activeObject == null && selectableList.size() > 0) {
            activeObject = selectableList.get(0);
            setActiveObject(activeObject);
        }

        selectableMap = new HashMap<>();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = transformTower.getSelectableList();
        collectSelectableTo(selectableMap, selectableList);

        selectableList = outputTower.getSelectableList();
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

}
