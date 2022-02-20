package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;

import java.util.List;
import java.util.Map;

public class AddTableFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        List<TableFx> tableFxList = transformTable.getFxList();
        TableFx tableFx = new TableFx(TableFunction.SORT, TableFunction.SORT.getName(), transformTable);
        tableFxList.add(tableFx);

        tableFx.setId(project.newUniqueId());

        step.getSelectableMap().put(tableFx.getSelectableId(), tableFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TABLE_FX, tableFx);

        /*Action Result*/
        action.getResultMap().put("tableFx", tableFx);
    }

}
