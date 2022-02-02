package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.AddColumnFx;
import com.tflow.model.editor.action.RequiredParamException;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;
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
    private EditorController editorCtl;

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
     */
    public void drawLines() {
        StringBuilder jsBuilder = new StringBuilder();

        jsBuilder.append("LeaderLine.positionByWindowResize = false;");
        for (Line line : step.getLineList()) {
            jsBuilder.append(line.getJsAdd());
        }
        jsBuilder.append("startup();");

        String javaScript = "$(function(){" + jsBuilder.toString() + "});";
        FacesUtil.runClientScript(javaScript);
    }

    /**
     * called from the client to
     * update all the lines that connected to this selectable object.
     */
    public void updateLines() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selectable = step.getSelectableMap().get(selectableId);

        StringBuilder jsBuilder = new StringBuilder();
        jsBuilder.append("lineStart();");
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
        jsBuilder.append("lineEnd();");
        jsBuilder.append("console.log('update lines connected to [" + selectableId + "] is successful.');");

        String javaScript = "$(function(){" + jsBuilder.toString() + "});";
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

    private Line getRequestedLine() {
        String startSelectableId = FacesUtil.getRequestParam("startSelectableId");
        if (startSelectableId == null) return null;

        String endSelectableId = FacesUtil.getRequestParam("endSelectableId");
        return new Line(startSelectableId, endSelectableId);
    }

    public void addLine() {
        Line newLine = getRequestedLine();
        addLine(newLine);
    }

    private void addLine(Line newLine) {
        StringBuilder jsBuilder = new StringBuilder();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        Selectable startSelectable = selectableMap.get(newLine.getStartSelectableId());
        Selectable endSelectable = selectableMap.get(newLine.getEndSelectableId());

        jsBuilder.append("lineStart();");
        if (endSelectable instanceof TransformColumn) {
            /*add line from Column to Column*/
            addLookup((DataColumn) startSelectable, (TransformColumn) endSelectable, jsBuilder);

        } else if (endSelectable instanceof DataFile) {
            /*add line from DataSource to DataFile*/
            addDataSourceLine((DataSource) startSelectable, (DataFile) endSelectable, jsBuilder);

        } else {
            log.error("addLine by unknown types(start:{},end:{})", startSelectable.getClass().getName(), endSelectable.getClass().getName());
        }
        jsBuilder.append("lineEnd();");

        String updateEm = "window.parent.updateEm('" + newLine.getStartSelectableId() + "');"
                + "window.parent.updateEm('" + newLine.getEndSelectableId() + "');";
        jsBuilder.append(updateEm);

        FacesUtil.runClientScript(jsBuilder.toString());
    }

    private void addDataSourceLine(DataSource dataSource, DataFile dataFile, StringBuilder jsBuilder) {
        String dataSourceId = ((Selectable) dataSource).getSelectableId();
        String dataFileId = dataFile.getSelectableId();
        log.warn("addDataSourceLine(dataSource:{}, dataFile:{})", dataSourceId, dataFileId);

        Line newLine = step.addLine(dataSourceId, dataFileId);
        jsBuilder.append(newLine.getJsAdd());
    }

    private void addLookup(DataColumn sourceColumn, TransformColumn transformColumn, StringBuilder jsBuilder) {
        log.warn("addLookup(sourceColumn:{}, targetColumn:{})", sourceColumn.getSelectableId(), transformColumn.getSelectableId());
        /*
         * 1. create ColumnFx and add to this step using Action
         * 2. create new line between sourceColumn and columnFx (call addLine again)
         * 3. remain line between columnFx and transformColumn to add by next statements
         */
        Project project = workspace.getProject();

        ColumnFx columnFx = new ColumnFx((DataColumn) transformColumn, ColumnFunction.LOOKUP, "Untitled", project.newElementId(), project.newElementId());

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);
        paramMap.put(CommandParamKey.STEP, this);
        paramMap.put(CommandParamKey.JAVASCRIPT_BUILDER, jsBuilder);

        try {
            new AddColumnFx(paramMap).execute();
        } catch (RequiredParamException e) {
            log.error("Add ColumnFx Failed!", e);
            FacesUtil.addError("Add ColumnFx Failed with Internal Command Error!");
            return;
        }

        /*editorCtl.selectObject(columnFx.getSelectableId());*/

        FacesUtil.addInfo("ColumnFx[" + columnFx.getName() + "] added.");
    }

    /**
     * Remove line when the client plug button is clicked.
     * ( the remove-button only shown on the single line plugged )
     * <p>
     * <br/><br/>
     * <b><i>Param:</i></b><br/>
     * String  selectableId<br/>
     * boolean isStartPlug | true = remove first line from start-plug, false = remove line from end-plug<br/>
     */
    public void removeLine() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        boolean isStartPlug = Boolean.parseBoolean(FacesUtil.getRequestParam("startPlug"));

        log.warn("removeLine(selectableId:{}, isStartPlug:{})", selectableId, isStartPlug);
        log.warn("lineList before remove = {}", step.getLineList().toArray());

        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) not found in current step", selectableId);
            return;
        }

        String friendSelectableId = "";
        Line line = null;
        if (isStartPlug) {
            line = selectable.getStartPlug().getLine();
            friendSelectableId = line.getEndSelectableId();
        } else {
            HasEndPlug hasEndPlug = (HasEndPlug) selectable;
            line = hasEndPlug.getEndPlug().getLine();
            friendSelectableId = line.getStartSelectableId();
        }

        log.warn("call step.removeLine()");
        step.removeLine(line);
        log.warn("lineList after remove = {}", step.getLineList().toArray());

        String javaScript = line.getJsRemove()
                + "window.parent.updateEm('" + selectableId + "');"
                + "window.parent.updateEm('" + friendSelectableId + "');";
        FacesUtil.runClientScript(javaScript);
    }

    public void extractData() {

    }

    public void transferData() {

    }
}
