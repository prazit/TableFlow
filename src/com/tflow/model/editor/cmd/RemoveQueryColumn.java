package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class RemoveQueryColumn extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        int columnId = (Integer) paramMap.get(CommandParamKey.COLUMN_ID);

        List<QueryColumn> columnList = query.getColumnList();
        QueryColumn column = null;
        for (int index = 0; index < columnList.size(); index++) {
            column = columnList.get(index);
            if (column.getId() == columnId) {
                columnList.remove(index);
            }
        }

        if (column == null) {
            throw new UnsupportedOperationException("column not found for column-id(" + columnId + ") in query '" + query.getName() + "'");
        }

        QueryTable queryTable = column.getOwner();
        for (QueryColumn queryColumn : queryTable.getColumnList()) {
            if (queryColumn.getId() == columnId) {
                queryColumn.setSelected(false);
                break;
            }
        }

        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        /*for Acion.executeUndo (AddQueryColumn)*/
        paramMap.put(CommandParamKey.QUERY_COLUMN, column);

        /*Action Result*/

        // save Column in Query data
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = mapper.toProjectUser(project);
        int queryId = query.getId();
        String childId = "" + queryId;
        dataManager.addData(ProjectFileType.QUERY, mapper.map(query), projectUser, queryId, step.getId(), childId);

        // save Column in Owner Table data
        dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(queryTable), projectUser, queryTable.getId(), step.getId(), childId);

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
