package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.List;
import java.util.Map;

public class AddTransformColumn extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        List<DataColumn> columnList = transformTable.getColumnList();
        TransformColumn transformColumn = new TransformColumn(columnList.size(), DataType.STRING, "Untitled", project.newElementId(), project.newElementId(), transformTable);
        columnList.add(transformColumn);

        transformColumn.setId(project.newUniqueId());

        step.getSelectableMap().put(transformColumn.getSelectableId(), transformColumn);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TRANSFORM_COLUMN, transformColumn);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TRANSFORM_COLUMN, transformColumn);
    }

}
