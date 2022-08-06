package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class RemoveTableFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TableFx tableFx = (TableFx) paramMap.get(CommandParamKey.TABLE_FX);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        TransformTable transformTable = tableFx.getOwner();
        List<TableFx> tableFxList = transformTable.getFxList();
        tableFxList.remove(tableFx);

        step.getSelectableMap().remove(tableFx.getSelectableId(), tableFx);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TABLE_FX, tableFx);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TABLE_FX, tableFx);

        // save Transformation data
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.TRANSFORMATION, (TWData) null, projectUser, tableFx.getId(), step.getId(), 0, tableFx.getId());

        // save Transformation list
        dataManager.addData(ProjectFileType.TRANSFORMATION_LIST, mapper.fromTableFxList(tableFxList), projectUser, tableFx.getId(), step.getId(), 0, tableFx.getId());

        // no line, tower, floor to save here
    }

}
