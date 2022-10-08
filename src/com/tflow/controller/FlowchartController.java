package com.tflow.controller;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.GroupData;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.FacesUtil;
import org.apache.kafka.common.utils.Java;
import org.mapstruct.factory.Mappers;

import javax.annotation.PostConstruct;
import javax.faces.application.ViewExpiredException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ViewScoped
@Named("flowchartCtl")
public class FlowchartController extends Controller {


    @Override
    public Page getPage() {
        return Page.EDITOR;
    }

    @Override
    public void onCreation() {
        createEventHandlers();
    }

    private void createEventHandlers() {
        /*TODO: need to sync height of floor in all towers (floor.minHeight = findRoomMaxHeightOnAFloor)
         * need "Tower.Event"
         */

        Step step = getStep();
        if (step.getIndex() < 0) {
            log.warn("FlowchartController.createEventHandlers: need to load step data before open step flowchart");
            return;
        }

        /*-- Handle all events --*/
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

        step.getOwner().getEventManager().addHandler(EventName.NAME_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();

                Project target = (Project) event.getTarget();
                String projectId = target.getId();
                ProjectGroup group;
                try {
                    group = target.getManager().loadProjectGroup(workspace, target.getGroupId());
                    ProjectItem targetProjectItem = group.getProjectList().stream().filter(projectItem -> projectItem.getId().compareTo(projectId) == 0).collect(Collectors.toList()).get(0);
                    targetProjectItem.setName(target.getName());
                } catch (Exception ex) {
                    String msg = "Project Name '" + target.getName() + "' is changed, but the name in group still unchanged by Internal Error!";
                    jsBuilder.pre(JavaScript.notiError, msg);
                    log.error(msg + ex.getMessage());
                    log.trace("", ex);
                    return;
                }

                propertyChanged(ProjectFileType.GROUP, group, property);
            }
        });

    }


    /*== Public Methods ==*/

    /**
     * Draw lines on the client when page loaded.
     */
    public void drawLines() {
        log.debug("drawLines:fromClient");
        jsBuilder.pre(JavaScript.preStartup);
        Step step = getStep();
        for (Line line : step.getLineList()) {
            jsBuilder.append(line.getJsAdd());
        }
        jsBuilder.append(JavaScript.postStartup)
                .runOnClient(true);
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
            jsBuilder.runOnClient();
        }
    }

    private void updateLines(JavaScriptBuilder jsBuilder, Selectable selectable) {
        Step step = getStep();
        List<Line> stepLineList = step.getLineList();
        if (selectable.getStartPlug() != null) {
            List<Line> lineList = getLineByStart(selectable.getSelectableId(), stepLineList);
            for (Line line : lineList) {
                /*remove old lines that start by this object*/
                jsBuilder.append(line.getJsRemove());
                /*add new one and put it back to the same position*/
                jsBuilder.append(line.getJsAdd());
            }
        }

        if (selectable instanceof HasEndPlug) {
            List<Line> lineList = getLineByEnd(selectable.getSelectableId(), stepLineList);
            for (Line line : lineList) {
                /*remove old lines that start by this object*/
                jsBuilder.append(line.getJsRemove());
                /*add new one and put it back to the same position*/
                jsBuilder.append(line.getJsAdd());
            }
        }
    }

    private List<Line> getLineByStart(String selectableId, List<Line> lineList) {
        List<Line> found = new ArrayList<>();
        for (Line line : lineList) {
            if (line.getStartSelectableId().equals(selectableId)) {
                found.add(line);
            }
        }
        return found;
    }

    private List<Line> getLineByEnd(String selectableId, List<Line> lineList) {
        List<Line> found = new ArrayList<>();
        for (Line line : lineList) {
            if (line.getEndSelectableId().equals(selectableId)) {
                found.add(line);
            }
        }
        return found;
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
            action = addDirectLine(newLine.getStartSelectableId(), newLine.getEndSelectableId(), false);
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
        jsBuilder.runOnClient();
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
        String friendSelectableId = null;
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

        Line line;
        if (isStartPlug) {
            line = selectable.getStartPlug().getLine();
            friendSelectableId = line.getEndSelectableId();
        } else {
            HasEndPlug hasEndPlug = (HasEndPlug) selectable;
            line = hasEndPlug.getEndPlug().getLine();
            friendSelectableId = line.getStartSelectableId();
        }
        Selectable friend = selectableMap.get(friendSelectableId);

        if (selectable instanceof ColumnFx) {
            removeColumnFx((ColumnFx) selectable);
        } else if (friend instanceof ColumnFx) {
            removeColumnFx((ColumnFx) friend);
        } else if (selectable instanceof TransformTable) {
            removeTransformTable((TransformTable) selectable);
        } else if (selectable instanceof DataTable) {
            removeDataTable((DataTable) selectable);
        } else {
            removeDirectLine(line, false);
        }

        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.pre(JavaScript.lineStart);
        jsBuilder.append(JavaScript.lineEnd);
        if (selectableMap.containsKey(selectableId)) {
            /*TODO: need to change to UpdateFloor by (floorIndex)
            Room room = (Room) selectable;
            jsBuilder.post(JavaScript.updateFl, room.getFloorIndex());*/
            jsBuilder.post(JavaScript.updateEm, selectableId);
        }
        if (friendSelectableId != null && selectableMap.containsKey(friendSelectableId)) {
            /*TODO: need to change to UpdateFloor by (floorIndex)
            Room room = (Room) friend;
            jsBuilder.post(JavaScript.updateFl, room.getFloorIndex());*/
            jsBuilder.post(JavaScript.updateEm, friendSelectableId);
        }
        jsBuilder.runOnClient();
    }

    private void removeDirectLine(Line line, boolean chain) {
        log.warn("removeDirectLine(line:{})", line);
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.LINE, line);
        paramMap.put(CommandParamKey.STEP, step);

        Action action = new RemoveDirectLine(paramMap);
        try {
            action.execute(chain);
        } catch (RequiredParamException e) {
            log.error("Remove Line Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Remove Line Failed with Internal Command Error!");
        }
    }

    private Action addDataSourceLine(DataSource dataSource, DataFile dataFile) {
        String dataSourceId = ((Selectable) dataSource).getSelectableId();
        String dataFileId = dataFile.getSelectableId();
        log.warn("addDataSourceLine(dataSource:{}, dataFile:{})", dataSourceId, dataFileId);
        return addDirectLine(dataSourceId, dataFileId, false);
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
            jsBuilder.pre(JavaScript.notiError, "Add ColumnFx Failed with Internal Command Error!");
            return action;
        }

        ColumnFx columnFx = (ColumnFx) action.getResultMap().get(ActionResultKey.COLUMN_FX);
        if (columnFx != null) {
            step.setActiveObject(columnFx);
            jsBuilder.post(JavaScript.notiInfo, "ColumnFx[" + columnFx.getName() + "] added.");
        }

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList)
                .post(JavaScript.refreshFlowChart)
                .runOnClient();

        return action;
    }

    private Action addDirectLine(String startSelectableId, String endSelectableId, boolean chain) {
        log.warn("addDirectLine(start:{}, end:{})", startSelectableId, endSelectableId);
        Step step = getStep();

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.LINE, new Line(startSelectableId, endSelectableId));
        paramMap.put(CommandParamKey.STEP, step);

        Action action = new AddDirectLine(paramMap);
        try {
            action.execute(chain);
        } catch (RequiredParamException e) {
            log.error("Add Line Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add Line Failed with Internal Command Error!");
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
            action.execute(false);
        } catch (RequiredParamException e) {
            log.error("Remove ColumnFx Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Remove ColumnFx Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(targetColumn);

        jsBuilder.post(JavaScript.notiInfo, "ColumnFx[" + columnFx.getName() + "] removed.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.post(JavaScript.refreshFlowChart);
        jsBuilder.runOnClient();
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
            jsBuilder.pre(JavaScript.notiError, "Extract Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(dataTable);

        if (log.isDebugEnabled()) log.debug("DataTable added, id:{}, name:'{}'", dataTable.getId(), dataTable.getName());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList)
                .post(JavaScript.refreshFlowChart)
                .runOnClient();
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
            jsBuilder.pre(JavaScript.notiError, "Transfer Data Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(transformTable);

        jsBuilder.post(JavaScript.notiInfo, "Table[" + transformTable.getName() + "] added.");

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList).post(JavaScript.refreshFlowChart).runOnClient();
    }

    /*TODO: need Action for RemoveColumn*/
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
            jsBuilder.pre(JavaScript.notiError, "Add Transform Column Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(transformColumn);

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, transformColumn.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, transformTable.getSelectableId());
        jsBuilder.post(JavaScript.notiInfo, "Column[" + transformColumn.getSelectableId() + "] added.");
        jsBuilder.runOnClient();
    }

    /*TODO: need Action for RemoveTransformation*/
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
            jsBuilder.pre(JavaScript.notiError, "Add Table Function Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(tableFx);

        jsBuilder.post(JavaScript.notiInfo, "TableFx[" + tableFx.getSelectableId() + "] added.");

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, tableFx.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, transformTable.getSelectableId());
        jsBuilder.runOnClient();
    }

    /*TODO: need Action for RemoveOutput*/
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

        OutputFile outputFile;
        try {
            Action action = new AddOutputFile(paramMap);
            action.execute();
            outputFile = (OutputFile) action.getResultMap().get(ActionResultKey.OUTPUT_FILE);
        } catch (RequiredParamException e) {
            log.error("Add Table Function Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Add Table Function Failed with Internal Command Error!");
            return;
        }

        step.setActiveObject(outputFile);

        jsBuilder.post(JavaScript.notiInfo, "OutputFile[" + outputFile.getSelectableId() + "] added.");

        jsBuilder.pre(JavaScript.selectAfterUpdateEm, outputFile.getSelectableId());
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.updateEm, dataTable.getSelectableId());
        jsBuilder.runOnClient();
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
            action.execute(false);
        } catch (RequiredParamException e) {
            log.error("Remove TransformTable Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Remove Transformation Table Failed with Internal Command Error!");
            return;
        }

        int sourceId = target.getSourceId();
        DataTable dataTable = step.getDataTable(sourceId);
        if (dataTable != null) {
            step.setActiveObject(dataTable);
        } else {
            log.warn("removeTransformTable: successful with warning! transformTable({}) contains invalid sourceId({})", selectableId, sourceId);
        }

        jsBuilder.post(JavaScript.notiInfo, "Table[" + target.getName() + "] is removed.");

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

        Action action;
        try {
            action = new RemoveDataTable(paramMap);
            action.execute(false);
        } catch (RequiredParamException e) {
            log.error("Remove DataTable Failed!", e);
            jsBuilder.pre(JavaScript.notiError, "Remove DataTable Failed with Internal Command Error!");
            return;
        }

        DataFile dataFile = (DataFile) action.getResultMap().get(ActionResultKey.DATA_FILE);
        if (dataFile != null) {
            step.setActiveObject(dataFile);
        }

        if (log.isDebugEnabled()) log.debug("DataTable removed: id:{}, name:{}, related-dataFile({}:{})", target.getId(), target.getName(), dataFile.getId(), dataFile.getName());

        /*TODO: need to change refreshFlowChart to updateAFloorInATower*/
        jsBuilder.pre(JavaScript.refreshStepList);
        jsBuilder.post(JavaScript.refreshFlowChart);
    }

    /**
     * Style Classes for flowchart container element
     */
    public String getStyleClass() {
        String styleClass = "";
        Step step = getStep();

        /*Table: Action Buttons: hide-actions */
        if (!step.isShowActionButtons()) styleClass += "hide-actions ";

        /*Table: Column Numbers: hide-numbers */
        if (!step.isShowColumnNumbers()) styleClass += "hide-numbers ";

        return styleClass;
    }
}
