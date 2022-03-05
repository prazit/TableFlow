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
    private Workspace workspace;

    private JavaScriptBuilder jsBuilder;

    @PostConstruct
    public void onCreation() {
        jsBuilder = new JavaScriptBuilder();
        createEventHandlers();
    }

    private void createEventHandlers() {
        /*TODO: need to sync height of floor in all towers (floor.minHeight = findRoomMaxHeightOnAFloor)
         * need "Tower.Event"
         */

        /*-- Handle all events --*/
        Step step = getStep();
        step.getEventManager()
                .removeHandlers(EventName.LINE_ADDED)
                .addHandler(EventName.LINE_ADDED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        Line line = (Line) event.getData();
                        jsBuilder.append(line.getJsAdd());
                    }
                })
                .removeHandlers(EventName.LINE_REMOVED)
                .addHandler(EventName.LINE_REMOVED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        Line line = (Line) event.getData();
                        jsBuilder.append(line.getJsRemove());
                    }
                });

        Map<String, Selectable> selectableMap = step.getSelectableMap();
        for (Selectable selectable : selectableMap.values()) {
            if (!(selectable instanceof HasEvent)) continue;
            HasEvent target = (HasEvent) selectable;

            if (target instanceof ColumnFx) {
                target.getEventManager()
                        .removeHandlers(EventName.REMOVE)
                        .addHandler(EventName.REMOVE, new EventHandler() {
                            @Override
                            public void handle(Event event) {
                                removeColumnFx((ColumnFx) event.getTarget());
                            }
                        });

            } else if (target instanceof DataTable) {
                if (target instanceof TransformTable) {
                    target.getEventManager()
                            .removeHandlers(EventName.REMOVE)
                            .addHandler(EventName.REMOVE, new EventHandler() {
                                @Override
                                public void handle(Event event) {
                                    removeTransformTable((TransformTable) event.getTarget());
                                }
                            });

                } else {
                    target.getEventManager()
                            .removeHandlers(EventName.REMOVE)
                            .addHandler(EventName.REMOVE, new EventHandler() {
                                @Override
                                public void handle(Event event) {
                                    removeDataTable((DataTable) event.getTarget());
                                }
                            });
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
        jsBuilder.pre(JavaScript.lineStart).pre(JavaScript.preStartup);
        Step step = getStep();
        for (Line line : step.getLineList()) {
            jsBuilder.append(line.getJsAdd());
        }
        jsBuilder.post(JavaScript.postStartup);

        FacesUtil.runClientScript(jsBuilder.toDeferString());
    }

    /**
     * called from the client to
     * update all the lines that connected to this selectable object.
     */
    public void updateLines() {
        String selectableId = FacesUtil.getRequestParam("selectableId");
        Step step = getStep();

        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("updateLines aborted: selectableId({}) not found in current step", selectableId);
            return;
        }

        /*Update lines of selectable*/
        updateLines(jsBuilder, selectable);

        /*Update lines of Child in DataTable(include TransformTable)*/
        if (selectable instanceof DataTable) {
            DataTable dataTable = (DataTable) selectable;
            for (DataColumn column : dataTable.getColumnList()) {
                updateLines(jsBuilder, column);
            }
            for (DataFile output : dataTable.getOutputList()) {
                updateLines(jsBuilder, output);
            }
        }

        /*Update lines of Child in TransformTable*/
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

        if (!jsBuilder.isEmpty()) {
            jsBuilder.pre(JavaScript.lineStart);
            jsBuilder.post(JavaScript.lineEnd);
            FacesUtil.runClientScript(jsBuilder.toString());
        }
    }

    private void updateLines(JavaScriptBuilder jsBuilder, Selectable selectable) {
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
        Step step = getStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        Selectable startSelectable = selectableMap.get(newLine.getStartSelectableId());
        Selectable endSelectable = selectableMap.get(newLine.getEndSelectableId());
        if (startSelectable == null || endSelectable == null) {
            log.error("addLine by null(start:{},end:{})", startSelectable, endSelectable);
            return;
        }

        Action action;
        if (endSelectable instanceof TransformColumn) {
            /*add line from Column to Column*/
            action = addColumnFx((DataColumn) startSelectable, (TransformColumn) endSelectable);

        } else if (endSelectable instanceof DataFile) {
            /*add line from DataSource to DataFile*/
            action = addDataSourceLine((DataSource) startSelectable, (DataFile) endSelectable);

        } else {
            /*add line from Column to ColumnFX*/
            action = addDirectLine(newLine.getStartSelectableId(), newLine.getEndSelectableId());
        }

        /*-- client-side javascript --*/

        List<Line> lineList = (List<Line>) action.getResultMap().get(ActionResultKey.LINE_LIST);
        //if (lineList != null) {
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.pre(JavaScript.lineStart);
        jsBuilder.post(JavaScript.lineEnd);
        for (Line line : lineList) {
            jsBuilder.post(JavaScript.updateEm, line.getStartSelectableId());
            jsBuilder.post(JavaScript.updateEm, line.getEndSelectableId());
        }
        //}
        FacesUtil.runClientScript(jsBuilder.toString());
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

        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Selectable selectable = selectableMap.get(selectableId);
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

        /*Remove object event may be occurred by unplugged listener. (Known event: RemoveColumnFx, RemoveDataTable, RemoveTransformTable)*/
        removeDirectLine(line);

        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.pre(JavaScript.lineStart);
        jsBuilder.append(JavaScript.lineEnd);
        if (selectableMap.containsKey(selectableId)) {
            jsBuilder.post(JavaScript.updateEm, selectableId);
        }
        if (selectableMap.containsKey(friendSelectableId)) {
            jsBuilder.post(JavaScript.updateEm, friendSelectableId);
        }
        FacesUtil.runClientScript(jsBuilder.toString());
    }

    private void removeDirectLine(Line line) {
        log.warn("removeDirectLine(line:{})", line);
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.LINE, line);
        paramMap.put(CommandParamKey.STEP, step);

        Action action = new RemoveDirectLine(paramMap);
        try {
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Remove Line Failed!", e);
            FacesUtil.addError("Remove Line Failed with Internal Command Error!");
        }
    }

    private Action addDataSourceLine(DataSource dataSource, DataFile dataFile) {
        String dataSourceId = ((Selectable) dataSource).getSelectableId();
        String dataFileId = dataFile.getSelectableId();
        log.warn("addDataSourceLine(dataSource:{}, dataFile:{})", dataSourceId, dataFileId);
        return addDirectLine(dataSourceId, dataFileId);
    }

    private Action addColumnFx(DataColumn sourceColumn, TransformColumn transformColumn) {
        log.warn("addLookup(sourceColumn:{}, targetColumn:{})", sourceColumn.getSelectableId(), transformColumn.getSelectableId());
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_COLUMN, sourceColumn);
        paramMap.put(CommandParamKey.TRANSFORM_COLUMN, transformColumn);
        paramMap.put(CommandParamKey.COLUMN_FUNCTION, ColumnFunction.LOOKUP);
        paramMap.put(CommandParamKey.STEP, step);
        //paramMap.put(CommandParamKey.JAVASCRIPT_BUILDER, jsBuilder);

        Action action = new AddColumnFx(paramMap);
        try {
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Add ColumnFx Failed!", e);
            FacesUtil.addError("Add ColumnFx Failed with Internal Command Error!");
        }

        ColumnFx columnFx = (ColumnFx) action.getResultMap().get(ActionResultKey.COLUMN_FX);
        if (columnFx != null) {
            step.setActiveObject(columnFx);
            FacesUtil.addInfo("ColumnFx[" + columnFx.getName() + "] added.");
        }

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.post(JavaScript.refreshFlowChart);

        return action;
    }

    private Action addDirectLine(String startSelectableId, String endSelectableId) {
        log.warn("addDirectLine(start:{}, end:{})", startSelectableId, endSelectableId);
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.LINE, new Line(startSelectableId, endSelectableId));
        paramMap.put(CommandParamKey.STEP, step);

        Action action = new AddDirectLine(paramMap);
        try {
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Add Line Failed!", e);
            FacesUtil.addError("Add Line Failed with Internal Command Error!");
        }

        return action;
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
        jsBuilder.post(JavaScript.refreshFlowChart);
        FacesUtil.runClientScript(jsBuilder.toString());
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
            dataTable = (DataTable) action.getResultMap().get(ActionResultKey.DATA_TABLE);
        } catch (Exception e) {
            log.error("Extract Data Failed!", e);
            FacesUtil.addError("Extract Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(dataTable);

        FacesUtil.addInfo("Table[" + dataTable.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList)
                .post(JavaScript.refreshFlowChart);
        FacesUtil.runClientScript(jsBuilder.toString());

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
            transformTable = (TransformTable) action.getResultMap().get(ActionResultKey.TRANSFORM_TABLE);
        } catch (RequiredParamException e) {
            log.error("Transfer Data Failed!", e);
            FacesUtil.addError("Transfer Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(transformTable);

        FacesUtil.addInfo("Table[" + transformTable.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.refreshFlowChart);
        FacesUtil.runClientScript(jsBuilder.toString());
    }

    public void addColumn() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        log.warn("addColumn(selectableId:{})", selectableId);

        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) is not found in current step", selectableId);
            return;
        }

        if (!(selectable instanceof TransformTable)) {
            log.error("addColumn only work on DataTable, {} is not allowed", selectable.getClass().getName());
            return;
        }

        TransformTable transformTable = (TransformTable) selectable;

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.STEP, step);

        TransformColumn transformColumn;
        try {
            Action action = new AddTransformColumn(paramMap);
            action.execute();
            transformColumn = (TransformColumn) action.getResultMap().get(ActionResultKey.TRANSFORM_COLUMN);
        } catch (RequiredParamException e) {
            log.error("Add Transform Column Failed!", e);
            FacesUtil.addError("Add Transform Column Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(transformColumn);

        FacesUtil.addInfo("Column[" + transformColumn.getSelectableId() + "] added.");

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, transformColumn.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, transformTable.getSelectableId());
        FacesUtil.runClientScript(jsBuilder.toString());
    }

    public void addTransformation() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        log.warn("addTransformation(selectableId:{})", selectableId);

        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) is not found in current step", selectableId);
            return;
        }

        if (!(selectable instanceof TransformTable)) {
            log.error("addTableFx only work on TransformTable, {} is not allowed", selectable.getClass().getName());
            return;
        }

        TransformTable transformTable = (TransformTable) selectable;

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.STEP, step);

        TableFx tableFx;
        try {
            Action action = new AddTableFx(paramMap);
            action.execute();
            tableFx = (TableFx) action.getResultMap().get(ActionResultKey.TABLE_FX);
        } catch (RequiredParamException e) {
            log.error("Add Table Function Failed!", e);
            FacesUtil.addError("Add Table Function Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(tableFx);

        FacesUtil.addInfo("TableFx[" + tableFx.getSelectableId() + "] added.");

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, tableFx.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, transformTable.getSelectableId());
        FacesUtil.runClientScript(jsBuilder.toString());
    }

    public void addOutputFile() {
        Step step = getStep();
        String selectableId = FacesUtil.getRequestParam("selectableId");
        log.warn("addOutputFile(selectableId:{})", selectableId);

        Selectable selectable = step.getSelectableMap().get(selectableId);
        if (selectable == null) {
            log.error("selectableId({}) is not found in current step", selectableId);
            return;
        }

        if (!(selectable instanceof DataTable)) {
            log.error("addTableFx only work on TransformTable, {} is not allowed", selectable.getClass().getName());
            return;
        }

        DataTable dataTable = (DataTable) selectable;

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.STEP, step);

        DataFile dataFile;
        try {
            Action action = new AddOutputFile(paramMap);
            action.execute();
            dataFile = (DataFile) action.getResultMap().get(ActionResultKey.DATA_FILE);
        } catch (RequiredParamException e) {
            log.error("Add Table Function Failed!", e);
            FacesUtil.addError("Add Table Function Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(dataFile);

        FacesUtil.addInfo("OutputFile[" + dataFile.getSelectableId() + "] added.");

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, dataFile.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, dataTable.getSelectableId());

        FacesUtil.runClientScript(jsBuilder.toString());
    }

    /**
     * Call from line listener.unplugged()
     */
    private void removeTransformTable(TransformTable target) {
        String selectableId = target.getSelectableId();
        log.warn("removeTransformTable(target:{})", selectableId);
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, target);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            Action action = new RemoveTransformTable(paramMap);
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Remove TransformTable Failed!", e);
            FacesUtil.addError("Remove Transformation Table Failed with Internal Command Error!");
            return;
        }

        String sourceSelectableId = target.getSourceSelectableId();
        DataTable dataTable = (DataTable) step.getSelectableMap().get(sourceSelectableId);
        if (dataTable != null) {
            step.setActiveObject(dataTable);
        } else {
            log.warn("removeTransformTable: successful with warning! transformTable({}) contains invalid sourceSelectableId({})", selectableId, sourceSelectableId);
        }

        FacesUtil.addInfo("Table[" + target.getName() + "] is removed.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.refreshFlowChart);
    }

    /**
     * Call from line listener.unplugged()
     */
    private void removeDataTable(DataTable target) {
        log.warn("removeDataTable(target:{})", target.getSelectableId());
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.DATA_TABLE, target);
        paramMap.put(CommandParamKey.STEP, step);

        try {
            Action action = new RemoveDataTable(paramMap);
            action.execute();
        } catch (RequiredParamException e) {
            log.error("Remove DataTable Failed!", e);
            FacesUtil.addError("Remove DataTable Failed with Internal Command Error!");
            return;
        }

        if (step.getActiveObject().getSelectableId().equals(target.getSelectableId())) {
            step.setActiveObject(target.getDataFile());
        }

        FacesUtil.addInfo("Table[" + target.getName() + "] is removed.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.refreshFlowChart);
    }

}
