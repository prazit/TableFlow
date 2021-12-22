package com.tflow.controller;

import com.tflow.model.editor.Line;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.FacesUtil;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ViewScoped
@Named("flowchartCtl")
public class FlowchartController extends Controller {

    @Inject
    private Workspace workspace;

    private Tower dataTower;
    private Tower transformTower;
    private Tower outputTower;
    private List<Line> lineList;
    private String javaScript;

    @PostConstruct
    public void onCreation() {
        Project project = workspace.getProject();
        Step step = project.getStepList().get(project.getActiveStepIndex());
        dataTower = step.getDataTower();
        transformTower = step.getTransformTower();
        outputTower = step.getOutputTower();
        lineList = step.getLineList();
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
        log.warn("getJavaScript = {}", javaScript);
        return javaScript;
    }

    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }
}
