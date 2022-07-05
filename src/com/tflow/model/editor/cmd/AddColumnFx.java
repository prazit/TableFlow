package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {
    private static final long serialVersionUID = 2022031309996660004L;

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
            columnFx = new ColumnFx(columnFunction, columnFunction.getName(), project.newElementId(), (DataColumn) targetColumn);
            columnFx.setId(project.newUniqueId());
            propertyMap = columnFx.getPropertyMap();
            initPropertyMap(propertyMap, sourceColumn);
        } else {
            /*Action 'RemoveColumnFx'.executeUndo*/
            propertyMap = columnFx.getPropertyMap();
            targetColumn = (TransformColumn) columnFx.getOwner();
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

        // save TransformColumn data
        ProjectDataManager projectDataManager = project.getManager();
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, columnFx, project, columnFx.getId(), step.getId(), 0, transformTable.getId());
        
        // no TransformColumnFx list to save here, it already saved in the AddTransformTable

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, line1, project, line1.getId(), step.getId());
        projectDataManager.addData(ProjectFileType.LINE, line2, project, line2.getId(), step.getId());

        // save Object(SourceColumn) at the startPlug of line1
        if (sourceColumn instanceof TransformColumn) {
            projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, sourceColumn, project, sourceColumn.getId(), step.getId(), 0, sourceColumn.getOwner().getId());
        } else {
            projectDataManager.addData(ProjectFileType.DATA_COLUMN, sourceColumn, project, sourceColumn.getId(), step.getId(), sourceColumn.getOwner().getId());
        }

        // save Object(TargetColumn) at the endPlug of line2
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, targetColumn, project, targetColumn.getId(), step.getId(), 0, transformTable.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, line1.getId(), step.getId());

        // no tower, floor to save here
    }

    private void initPropertyMap(Map<String, Object> propertyMap, DataColumn sourceColumn) {
        propertyMap.put("sourceTable", sourceColumn.getOwner().getSelectableId());
        propertyMap.put("sourceColumn", sourceColumn.getSelectableId());
    }

}
