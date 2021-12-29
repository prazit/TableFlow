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
    private Step step;
    private Map<String, Selectable> selectableMap;

    @PostConstruct
    public void onCreation() {
        Project project = workspace.getProject();
        step = project.getActiveStep();
        selectableMap = collectSelectableToMap();
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    private Map<String, Selectable> collectSelectableToMap() {

        List<Selectable> selectableList = step.getDataTower().getSelectableList();
        Selectable activeObject = step.getActiveObject();
        if (activeObject == null && selectableList.size() > 0) {
            activeObject = selectableList.get(0);
            step.setActiveObject(activeObject);
        }

        Map<String, Selectable> map = new HashMap<>();
        collectSelectableTo(map, selectableList);

        selectableList = step.getTransformTower().getSelectableList();
        collectSelectableTo(map, selectableList);

        selectableList = step.getOutputTower().getSelectableList();
        collectSelectableTo(map, selectableList);

        return map;
    }

    private void collectSelectableTo(Map<String, Selectable> map, List<Selectable> selectableList) {
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
    }

    /*== Public Methods ==*/

    /**
     * Get active class for css.
     * @return " active" or empty string
     */
    public String active(Selectable selectable) {
        return (selectable.getSelectableId().compareTo(step.getActiveObject().getSelectableId()) == 0) ? " active" : "";
    }

    /**
     * Set active object from client.
     */
    public void selectObject() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selected = selectableMap.get(selectableId);
        if (selected == null) {
            log.warn("selectableMap not contains selectableId={}", selectableId);
            /*throw new IllegalStateException("selectableMap not contains selectableId=" + selectableId);*/
        } else {
            step.setActiveObject(selected);
        }
    }

    public void addLine() {

        Line singleLine = getRequestedLine();

        List<Line> lineList = step.getLineList();
        int index = 0;
        if (singleLine != null) {
            lineList.add(singleLine);
            index = lineList.size();
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

        String javaScript = "$(function(){" + builder.toString() + ";startup();});";
        FacesUtil.runClientScript(javaScript);
    }

    private Line getRequestedLine() {
        String startPlug = FacesUtil.getRequestParam("startPlug");
        if(startPlug == null) return null;

        String endPlug = FacesUtil.getRequestParam("endPlug");
        String lineType = FacesUtil.getRequestParam("lineType");
        return new Line(startPlug,endPlug,LineType.valueOf(lineType.toUpperCase()));
    }

}
