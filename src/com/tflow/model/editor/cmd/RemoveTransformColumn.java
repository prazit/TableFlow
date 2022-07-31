package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
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

public class RemoveTransformColumn extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformColumn transformColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        TransformTable transformTable = (TransformTable) transformColumn.getOwner();
        List<DataColumn> columnList = transformTable.getColumnList();
        columnList.remove(transformColumn);

        step.getSelectableMap().remove(transformColumn.getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TRANSFORM_COLUMN, transformColumn);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TRANSFORM_COLUMN, transformColumn);

        // save TransformColumn data
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, (TWData) null, projectUser, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // save TransformColumn list
        dataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), projectUser, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // no line, tower, floor to save here
    }

}
