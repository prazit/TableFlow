package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.FacesUtil;
import org.jboss.weld.manager.Transform;

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

    /**
     * Draw lines on the client when page loaded.
     * Or add new line from the client.
     */
    public void addLine() {
        Line newLine = getRequestedLine();
        StringBuilder jsBuilder = new StringBuilder();

        if (newLine == null) {
            for (Line line : step.getLineList()) {
                jsBuilder.append(line.getJsAdd());
            }
        } else {
            step.addLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
            jsBuilder.append(newLine.getJsAdd());
        }

        String javaScript = "$(function(){" + jsBuilder.toString() + ";startup();});";
        FacesUtil.runClientScript(javaScript);
    }

    private Line getRequestedLine() {
        String startSelectableId = FacesUtil.getRequestParam("startSelectableId");
        if (startSelectableId == null) return null;

        String endSelectableId = FacesUtil.getRequestParam("endSelectableId");
        return new Line(startSelectableId, endSelectableId);
    }

    /**
     * register selectable object from the client to
     * update all the lines that connected to this selectable object.
     */
    public void updateLines() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selectable = step.getSelectableMap().get(selectableId);

        StringBuilder jsBuilder = new StringBuilder();
        updateLines(jsBuilder, selectable);

        /*in case of DataTable need to redraw lines of all columns and outputs*/
        if (selectable instanceof DataTable) {
            DataTable dataTable = (DataTable) selectable;
            for (DataColumn column : dataTable.getColumnList()) {
                updateLines(jsBuilder, column);
            }
            for (DataFile output : dataTable.getOutputList()) {
                updateLines(jsBuilder, output);
            }

        }

        /*in case of TransformTable need to redraw lines of all columnFX and tableFX*/
        if (selectable instanceof TransformTable) {
            TransformTable transformTable = (TransformTable) selectable;
            for (DataColumn column : transformTable.getColumnList()) {
                ColumnFx fx = ((TransformColumn) column).getFx();
                if (fx != null) updateLines(jsBuilder, fx);
            }
            for (TableFx tableFx : transformTable.getFxList()) {
                updateLines(jsBuilder, tableFx);
            }
        }

        String javaScript = "$(function(){" + jsBuilder.toString() + ";window.parent.zoomEnd();console.log('register to the server is successful.');});";
        FacesUtil.runClientScript(javaScript);
    }

    private void updateLines(StringBuilder jsBuilder, Selectable selectable) {
        if (selectable.getStartPlug() != null) {
            List<Line> lineList = step.getLineByStart(selectable.getSelectableId());
            for (Line line : lineList) {
                /*remove old lines that start by this object*/
                jsBuilder.append(line.getJsRemove());
                /*add new one and put it back to the same position*/
                jsBuilder.append(line.getJsAdd());
            }
        }

        if (selectable instanceof HasEndPlug) {
            List<Line> lineList = step.getLineByEnd(selectable.getSelectableId());
            for (Line line : lineList) {
                /*remove old lines that start by this object*/
                jsBuilder.append(line.getJsRemove());
                /*add new one and put it back to the same position*/
                jsBuilder.append(line.getJsAdd());
            }
        }
    }

}
