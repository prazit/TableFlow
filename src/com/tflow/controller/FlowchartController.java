package com.tflow.controller;

import com.tflow.HasEvent;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.util.FacesUtil;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

    @PostConstruct
    public void onCreation() {
        createEventHandlers();
    }

    private void createEventHandlers() {

        /*TODO: need to sync height of floor in all towers (floor.minHeight = findRoomMaxHeightOnAFloor)*/

        /*-- Handle all events --*/
        Step step = getStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        for (Selectable selectable : selectableMap.values()) {
            if (!(selectable instanceof HasEvent)) continue;
            HasEvent target = (HasEvent) selectable;

            if (target instanceof ColumnFx) {
                target.getEventManager().addHandler(EventName.REMOVE, new EventHandler(selectable) {
                    @Override
                    public void handle(Event event) {
                        removeColumnFx((ColumnFx) this.target);
                    }
                });

            } else if (target instanceof DataTable) {
                if (target instanceof TransformTable) {
                    /*TODO: event TransformTable.REMOVE: execute action 'RemoveTransformTable'.*/

                } else {
                    /*TODO: event DataTable.REMOVE: execute action 'RemoveDataTable'.*/

                }
            }
        }
    }

    public Step getStep() {
        return workspace.getProject().getActiveStep();
    }

    /*== Public Methods ==*/

    /**
     * Get active class for css.
     *
     * @return " active" or empty string
     */
    public String active(Selectable selectable) {
        Step step = getStep();
        Selectable activeObject = step.getActiveObject();
        return (activeObject != null && selectable.getSelectableId().compareTo(activeObject.getSelectableId()) == 0) ? " active" : "";
    }

    /**
     * Draw lines on the client when page loaded.
     */
    public void drawLines() {
        StringBuilder jsBuilder = new StringBuilder();

        jsBuilder.append("LeaderLine.positionByWindowResize = false;");
        Step step = getStep();
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
        Step step = getStep();
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
        Step step = getStep();
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
        log.warn("getRequestedLine(startSelectableId:{}, endSelectableId:{})", startSelectableId, endSelectableId);
        return new Line(startSelectableId, endSelectableId);
    }

    public void addLine() {
        Line newLine = getRequestedLine();
        addLine(newLine);
    }

    private void addLine(Line newLine) {
        StringBuilder jsBuilder = new StringBuilder();
        Step step = getStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        Selectable startSelectable = selectableMap.get(newLine.getStartSelectableId());
        Selectable endSelectable = selectableMap.get(newLine.getEndSelectableId());
        if (startSelectable == null || endSelectable == null) {
            log.error("addLine by null(start:{},end:{})", startSelectable, endSelectable);
            return;
        }

        jsBuilder.append("lineStart();");
        if (endSelectable instanceof TransformColumn) {
            /*add line from Column to Column*/
            addLookup((DataColumn) startSelectable, (TransformColumn) endSelectable, jsBuilder);
            return;

        } else if (endSelectable instanceof DataFile) {
            /*add line from DataSource to DataFile*/
            addDataSourceLine((DataSource) startSelectable, (DataFile) endSelectable, jsBuilder);

        } else {
            newLine = step.addLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
            jsBuilder.append(newLine.getJsAdd());
            log.warn("addLine by unknown types(start:{},end:{})", startSelectable.getClass().getName(), endSelectable.getClass().getName());

            jsBuilder.append("lineEnd();");

            String updateEm = "window.parent.updateEm('" + newLine.getStartSelectableId() + "');"
                    + "window.parent.updateEm('" + newLine.getEndSelectableId() + "');";
            jsBuilder.append(updateEm);
        }

        FacesUtil.runClientScript(jsBuilder.toString());
    }

    private void addDataSourceLine(DataSource dataSource, DataFile dataFile, StringBuilder jsBuilder) {
        String dataSourceId = ((Selectable) dataSource).getSelectableId();
        String dataFileId = dataFile.getSelectableId();
        log.warn("addDataSourceLine(dataSource:{}, dataFile:{})", dataSourceId, dataFileId);

        Step step = getStep();
        Line newLine = step.addLine(dataSourceId, dataFileId);
        jsBuilder.append(newLine.getJsAdd());
    }

    private void addLookup(DataColumn sourceColumn, TransformColumn transformColumn, StringBuilder jsBuilder) {
        log.warn("addLookup(sourceColumn:{}, targetColumn:{})", sourceColumn.getSelectableId(), transformColumn.getSelectableId());
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_COLUMN, sourceColumn);
        paramMap.put(CommandParamKey.TRANSFORM_COLUMN, transformColumn);
        paramMap.put(CommandParamKey.COLUMN_FUNCTION, ColumnFunction.LOOKUP);
        paramMap.put(CommandParamKey.STEP, step);
        //paramMap.put(CommandParamKey.JAVASCRIPT_BUILDER, jsBuilder);

        ColumnFx columnFx;
        try {
            Action action = new AddColumnFx(paramMap);
            action.execute();
            columnFx = (ColumnFx) action.getResultMap().get("columnFx");
        } catch (RequiredParamException e) {
            log.error("Add ColumnFx Failed!", e);
            FacesUtil.addError("Add ColumnFx Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(columnFx);

        FacesUtil.addInfo("ColumnFx[" + columnFx.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    private void removeColumnFx(ColumnFx columnFx) {
        DataColumn targetColumn = columnFx.getOwner();
        log.warn("removeColumnFx(targetColumn:{})", targetColumn.getName());
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);
        paramMap.put(CommandParamKey.STEP, step);
        //paramMap.put(CommandParamKey.JAVASCRIPT_BUILDER, jsBuilder);

        Action action = new RemoveColumnFx(paramMap);
        try {
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Remove ColumnFx Failed!", e);
            FacesUtil.addError("Remove ColumnFx Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(targetColumn);

        FacesUtil.addInfo("ColumnFx[" + columnFx.getName() + "] removed.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    /**
     * Remove line when the client plug button is clicked.
     * <p>
     * <br/><br/>
     * <b><i>Param:</i></b><br/>
     * String  selectableId<br/>
     * boolean isStartPlug | true = remove first line from start-plug, false = remove line from end-plug<br/>
     */
    public void removeLine() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        boolean isStartPlug = Boolean.parseBoolean(FacesUtil.getRequestParam("startPlug"));
        Step step = getStep();

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

    /**
     * Extract Data Structure from DataFile when the client plug button is clicked.
     */
    public void extractData() {
        String selectableId = FacesUtil.getRequestParam("selectableId");

        Step step = getStep();
        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) not found in current step", selectableId);
            return;
        }

        if (!(selectable instanceof DataFile)) {
            log.error("extractData only work on DataFile, {} is not allowed", selectable.getClass().getName());
            return;
        }

        DataFile dataFile = (DataFile) selectable;

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);
        paramMap.put(CommandParamKey.STEP, step);

        Action action;
        DataTable dataTable;
        try {
            action = new AddDataTable(paramMap);
            action.execute();
            dataTable = (DataTable) action.getResultMap().get("dataTable");
        } catch (Exception e) {
            log.error("Extract Data Failed!", e);
            FacesUtil.addError("Extract Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(dataTable);

        FacesUtil.addInfo("Table[" + dataTable.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript("refreshFlowChart();");

        /*TODO: issue: after refresh, the activeObject is not dataTable*/
    }

    /**
     * Transfer Data from another Data-Table when the client plug button is clicked.
     * Note: this look like duplicate the table.
     */
    public void transferData() {
        String selectableId = FacesUtil.getRequestParam("selectableId");

        Step step = getStep();
        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) not found in current step", selectableId);
            return;
        }

        if (!(selectable instanceof DataTable)) {
            log.error("transferData only work on DataTable, {} is not allowed", selectable.getClass().getName());
            return;
        }

        DataTable dataTable = (DataTable) selectable;

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.STEP, step);

        TransformTable transformTable;
        try {
            Action action = new AddTransformTable(paramMap);
            action.execute();
            transformTable = (TransformTable) action.getResultMap().get("transformTable");
        } catch (RequiredParamException e) {
            log.error("Transfer Data Failed!", e);
            FacesUtil.addError("Transfer Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(transformTable);

        FacesUtil.addInfo("Table[" + transformTable.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        FacesUtil.runClientScript("refreshFlowChart();");
    }

    public void addColumn() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selectable = step.getSelectableMap().get(selectableId);
        log.warn("addColumn(dataTable:{})", selectable.getSelectableId());
        /*TODO: addColumn for data-table & transofm-table*/
    }

    public void addColumnFx() {
        /*TODO: addColumnFx for transofm-table*/
    }

    public void addTransformation() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selectable = step.getSelectableMap().get(selectableId);
        log.warn("addTransformation(dataTable:{})", selectable.getSelectableId());
        /*TODO: addTransformation for transform-table*/
    }

    public void addOutputFile() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Selectable selectable = step.getSelectableMap().get(selectableId);
        log.warn("addOutputFile(dataTable:{})", selectable.getSelectableId());
        /*TODO: addOutputFile for data-table and transform-table*/
    }

}
