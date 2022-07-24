package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        Map<String, Object> propertyMap;
        DataColumn sourceColumn;
        TransformColumn targetColumn;
        ColumnFunction columnFunction;

        /*support undo command of Action 'RemoveColumnFx'*/
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        boolean isExecute = (columnFx == null);
        if (isExecute) {
            /*Action 'AddColumnFx'.execute*/
            targetColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
            sourceColumn = (DataColumn) paramMap.get(CommandParamKey.DATA_COLUMN);
            columnFunction = (ColumnFunction) paramMap.get(CommandParamKey.COLUMN_FUNCTION);
            columnFx = new ColumnFx(columnFunction, columnFunction.getName(), project.newElementId(), targetColumn);
            columnFx.setId(project.newUniqueId());
            columnFx.setOwner(targetColumn);
            targetColumn.setFx(columnFx);
            propertyMap = columnFx.getPropertyMap();
            initPropertyMap(propertyMap, sourceColumn);
        } else {
            /*Action 'RemoveColumnFx'.executeUndo*/
            propertyMap = columnFx.getPropertyMap();
            targetColumn = (TransformColumn) columnFx.getOwner();
            targetColumn.setFx(null);
            columnFx.setOwner(null);
            String sourceSelectableId = (String) propertyMap.get("sourceColumn");
            sourceColumn = (DataColumn) selectableMap.get(sourceSelectableId);
            columnFunction = columnFx.getFunction();
        }

        TransformTable transformTable = (TransformTable) targetColumn.getOwner();
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.add(columnFx);
        columnFxList.sort(Comparator.comparingInt(columnFx2 -> columnFx2.getOwner().getIndex()));

        selectableMap.put(columnFx.getSelectableId(), columnFx);

        List<Line> lineList = new ArrayList<>();
        /*Notice: draw lines below tested on ColumnFunction.LOOKUP and expect to work for all ColumnFunction*/

        /*line between sourceColumn and columnFx*/
        Line line1 = step.addLine(sourceColumn.getSelectableId(), columnFx.getSelectableId());
        line1.setId(project.newUniqueId());

        /*line between columnFx and targetColumn*/
        Line line2 = step.addLine(columnFx.getSelectableId(), targetColumn.getSelectableId());
        line2.setId(project.newUniqueId());

        lineList.add(line1);
        lineList.add(line2);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*Action Result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.LINE_LIST, lineList);
        resultMap.put(ActionResultKey.COLUMN_FX, columnFx);

        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;

        // save TransformColumnFx data
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, mapper.map(columnFx), project, columnFx.getId(), step.getId(), 0, transformTable.getId());

        // no TransformColumnFx list to save here, it already saved in the AddTransformTable

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, mapper.map(line1), project, line1.getId(), step.getId());
        projectDataManager.addData(ProjectFileType.LINE, mapper.map(line2), project, line2.getId(), step.getId());

        // save Object(SourceColumn) at the startPlug of line1
        if (sourceColumn instanceof TransformColumn) {
            projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) sourceColumn), project, sourceColumn.getId(), step.getId(), 0, sourceColumn.getOwner().getId());
        } else {
            projectDataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(sourceColumn), project, sourceColumn.getId(), step.getId(), sourceColumn.getOwner().getId());
        }

        // save TransformColumn data (TargetColumn) at the endPlug of line2
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map(targetColumn), project, targetColumn.getId(), step.getId(), 0, transformTable.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, line1.getId(), step.getId());

        // no tower, floor to save here

        // save Step data: need to update Step record every Line added*/
        projectDataManager.addData(ProjectFileType.STEP, mapper.map(step), project, step.getId(), step.getId());

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

    private void initPropertyMap(Map<String, Object> propertyMap, DataColumn sourceColumn) {
        propertyMap.put("sourceTable", sourceColumn.getOwner().getSelectableId());
        propertyMap.put("sourceColumn", sourceColumn.getSelectableId());
    }

}
