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

        TableFx tableFx = (TableFx) paramMap.get(CommandParamKey.TABLE_FX);
        if(tableFx == null) {
            // for AddTableFx
            tableFx = new TableFx(TableFunction.SORT, TableFunction.SORT.getName(), transformTable);
            tableFx.setId(project.newUniqueId());
        }/*else{
            // nothing for RemoveTableFx.Undo
        }*/

        List<TableFx> tableFxList = transformTable.getFxList();
        tableFxList.add(tableFx);

        step.getSelectableMap().put(tableFx.getSelectableId(), tableFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TABLE_FX, tableFx);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TABLE_FX, tableFx);

        // save Transformation data
        ProjectDataManager projectDataManager = project.getManager();
        projectDataManager.addData(ProjectFileType.TRANSFORMATION, tableFx, step.getOwner(), tableFx.getId(), step.getId(), 0, transformTable.getId());

        // save Transformation list
        projectDataManager.addData(ProjectFileType.TRANSFORMATION_LIST, tableFxList, step.getOwner(), tableFx.getId(), step.getId(), 0, transformTable.getId());

        // no line, tower, floor to save here
    }

}
