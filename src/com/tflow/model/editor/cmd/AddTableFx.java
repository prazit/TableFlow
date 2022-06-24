package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.List;
import java.util.Map;

public class AddTableFx extends Command {
    private static final long serialVersionUID = 2022031309996660017L;

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
        action.getResultMap().put(ActionResultKey.TABLE_FX, tableFx);

        // save Transformation data
        ProjectDataManager.addData(ProjectFileType.TRANSFORMATION, tableFx, step.getOwner(), tableFx.getId(), step.getId(), 0, transformTable.getId());

        // save Transformation list
        ProjectDataManager.addData(ProjectFileType.TRANSFORMATION_LIST, tableFxList, step.getOwner(), tableFx.getId(), step.getId(), 0, transformTable.getId());

    }

}
