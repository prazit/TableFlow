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

    @PostConstruct
    public void onCreation() {
        Project project = workspace.getProject();
        step = project.getActiveStep();
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    /*== Public Methods ==*/

    /**
     * Get active class for css.
     *
     * @return " active" or empty string
     */
    public String active(Selectable selectable) {
        Selectable activeObject = step.getActiveObject();
        return (activeObject != null && selectable.getSelectableId().compareTo(activeObject.getSelectableId()) == 0) ? " active" : "";
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
        if (startPlug == null) return null;

        String endPlug = FacesUtil.getRequestParam("endPlug");
        String lineType = FacesUtil.getRequestParam("lineType");
        return new Line(startPlug, endPlug, LineType.valueOf(lineType.toUpperCase()));
    }

}
