package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.FacesUtil;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("flowchartCtl")
public class FlowchartController extends Controller {

    @Inject
    private Workspace workspace;

    private boolean enabled;

    private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;
    private List<Line> lineList;
    private String javaScript;

    private Selectable activeObject;
    private Map<String, Selectable> selectableMap;

    @PostConstruct
    public void onCreation() {
        Project project = workspace.getProject();
        Step step = project.getActiveStep();
        enabled = step != null;
        if (enabled) {
            dataTower = step.getDataTower();
            transformTower = step.getTransformTower();
            outputTower = step.getOutputTower();
            lineList = step.getLineList();
            activeObject = step.getActiveObject();
            selectableMap = collectSelectableToMap();
        }
    }

    private Map<String, Selectable> collectSelectableToMap() {
        Map<String, Selectable> map = new HashMap<>();

        List<Selectable> selectableList = dataTower.getSelectableList();
        if (activeObject == null && selectableList.size() > 0) {
            activeObject = selectableList.get(0);
        }

        for (Selectable selectable : selectableList) {
            map.put(selectable.getSelectableId(), selectable);
            if (selectable instanceof DataTable) {
                DataTable dt = (DataTable) selectable;

                for (DataColumn column : dt.getColumnList()) {
                    map.put(column.getSelectableId(), column);
                }

                for (DataOutput output : dt.getOutputList()) {
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

        return map;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

    public String getJavaScript() {
        return javaScript;
    }

    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }

    public Selectable getActiveObject() {
        return activeObject;
    }

    public void setActiveObject(Selectable activeObject) {
        this.activeObject = activeObject;
    }

    public String active(Selectable selectable) {
        String active = (selectable.getSelectableId().compareTo(activeObject.getSelectableId()) == 0) ? " active" : "";
        return active;
    }

    public void selectObject() {
        String objectId = FacesUtil.getRequestParam("selectableId");
        Selectable selected = selectableMap.get(objectId);
        if (selected != null) {
            setActiveObject(selected);
            workspace.getProject().getActiveStep().setActiveObject(selected);
        }
    }

    public void addLine(Line singleLine) {
        List<Line> lineList;
        int index = 0;
        if (singleLine == null) {
            lineList = this.lineList;
        } else {
            lineList = Arrays.asList(singleLine);
            index = this.lineList.size();
            this.lineList.add(singleLine);
        }

        StringBuilder builder = new StringBuilder();
        for (Line line : lineList) {
            builder.append(String.format("lines[%d] = new LeaderLine(document.getElementById('%s'), document.getElementById('%s'), %s);",
                    index++,
                    line.getStartPlug(),
                    line.getEndPlug(),
                    line.getType().getJsVar()
            ));
        }

        javaScript = "$(function(){" + builder.toString() + "});";
        FacesUtil.runClientScript(javaScript);
    }

}
