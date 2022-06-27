package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveColumnFx extends Command {
    private static final long serialVersionUID = 2022031309996660008L;

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        ColumnFx columnFx = (ColumnFx) paramMap.get(CommandParamKey.COLUMN_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        DataColumn targetColumn = columnFx.getOwner();
        TransformTable transformTable = (TransformTable) targetColumn.getOwner();

        /*remove remaining lines on startPlug*/
        LinePlug startPlug = columnFx.getStartPlug();
        List<Line> removedLineList = new ArrayList<>(startPlug.getLineList());
        step.removeLine(startPlug);

        /*remove remaining line on endPlug*/
        for (ColumnFxPlug endPlug : columnFx.getEndPlugList()) {
            removedLineList.addAll(endPlug.getLineList());
            step.removeLine(endPlug);
        }

        /*remove fx from FxTable*/
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.remove(columnFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*no Action Result*/

        // save TransformColumnFx data
        Project project = step.getOwner();
        ProjectDataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, null, project, columnFx.getId(), step.getId(), 0, transformTable.getId());

        // no TransformColumnFx list to save here, it already saved in the AddTransformTable

        // save Line data
        for (Line line : removedLineList) {
            ProjectDataManager.addData(ProjectFileType.LINE, null, project, line.getId(), step.getId());
        }

        // save Line list
        ProjectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, 1, step.getId());

        // no tower, floor to save here
    }

}
