package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;

import java.util.List;
import java.util.Map;

public class RemoveTransformColumn extends Command {
    private static final long serialVersionUID = 2022031309996660013L;

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
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN, (TWData) null, project, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // save TransformColumn list
        projectDataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), project, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // no line, tower, floor to save here
    }

}
