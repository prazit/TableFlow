package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.mapper.ProjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataColumn targetColumn = columnFx.getOwner();
        TransformTable transformTable = (TransformTable) targetColumn.getOwner();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        /*remove remaining lines on startPlug*/
        LinePlug startPlug = columnFx.getStartPlug();
        List<Line> removedLineList = new ArrayList<>(startPlug.getLineList());
        step.removeLine(startPlug);

        /*remove remaining line on endPlug*/
        List<DataColumn> updatedColumnList = new ArrayList<>();
        for (ColumnFxPlug endPlug : columnFx.getEndPlugList()) {
            Line line = endPlug.getLine();
            removedLineList.add(line);
            updatedColumnList.add((DataColumn) selectableMap.get(line.getStartSelectableId()));
            step.removeLine(endPlug);
        }

        /*remove fx from FxTable*/
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.remove(columnFx);

        selectableMap.remove(columnFx.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*no Action Result*/

        // save TransformColumnFx data
        Project project = step.getOwner();
        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, (TWData) null, project, columnFx.getId(), step.getId(), 0, transformTable.getId());

        // no TransformColumnFx list to save here, it already saved in the AddTransformTable

        // save Line data
        for (Line line : removedLineList) {
            projectDataManager.addData(ProjectFileType.LINE, (TWData) null, project, line.getId(), step.getId());
        }

        // save Object(TransformColumn) at startPlug.
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) targetColumn), project, targetColumn.getId(), step.getId(), 0, transformTable.getId());

        // save Objects(DataColumn) at endPlug.
        for (DataColumn dataColumn : updatedColumnList) {
            if (dataColumn instanceof TransformColumn) {
                projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map((TransformColumn) dataColumn), project, dataColumn.getId(), step.getId(), 0, dataColumn.getOwner().getId());
            } else {
                projectDataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), project, dataColumn.getId(), step.getId(), dataColumn.getOwner().getId());
            }
        }

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, 1, step.getId());

        // no tower, floor to save here
    }

}
