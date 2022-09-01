package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
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

public class AddTransformColumn extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Project project = step.getOwner();

        List<DataColumn> columnList = transformTable.getColumnList();
        TransformColumn transformColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
        if(transformColumn == null) {
            // for AddTransformColumn
            transformColumn = new TransformColumn(columnList.size(), DataType.STRING, "Untitled", ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), transformTable);
            transformColumn.setId(ProjectUtil.newUniqueId(project));
        }/*else{
            //nothing for RemoveTransformColumn.Undo
        }*/

        columnList.add(transformColumn);

        step.getSelectableMap().put(transformColumn.getSelectableId(), transformColumn);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.TRANSFORM_COLUMN, transformColumn);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.TRANSFORM_COLUMN, transformColumn);

        // save TransformColumn data
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map(transformColumn), projectUser, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // save TransformColumn list
        dataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), projectUser, transformColumn.getId(), step.getId(), 0, transformTable.getId());

        // no line, tower, floor to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
