package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class AddTableFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        TableFx tableFx = (TableFx) paramMap.get(CommandParamKey.TABLE_FX);
        if (tableFx == null) {
            // for AddTableFx
            tableFx = new TableFx(TableFunction.SORT, TableFunction.SORT.getName(), transformTable);
            tableFx.setId(ProjectUtil.newUniqueId(project));
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
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.TRANSFORMATION, mapper.map(tableFx), projectUser, tableFx.getId(), step.getId(), 0, transformTable.getId());

        // save Transformation list
        dataManager.addData(ProjectFileType.TRANSFORMATION_LIST, mapper.fromTableFxList(tableFxList), projectUser, tableFx.getId(), step.getId(), 0, transformTable.getId());

        // no line, tower, floor to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
    }

}
