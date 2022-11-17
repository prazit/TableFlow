package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.query.ColumnType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class AddQueryColumn extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        QueryColumn queryColumn = (QueryColumn) paramMap.get(CommandParamKey.QUERY_COLUMN);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        Project project = step.getOwner();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        List<QueryColumn> columnList = query.getColumnList();

        int columnId = queryColumn.getId();
        String columnName = queryColumn.getName();
        if (exists(columnName, columnList)) {
            throw new UnsupportedOperationException("Column name '" + columnName + "' already exists");
        }

        QueryColumn selectedColumn = new QueryColumn();
        selectedColumn.setId(columnId);
        selectedColumn.setIndex(columnList.size());
        selectedColumn.setSelected(true);
        selectedColumn.setType(ColumnType.NORMAL);
        selectedColumn.setName(columnName);
        selectedColumn.setValue(queryColumn.getValue());
        selectedColumn.setOwner(queryColumn.getOwner());
        columnList.add(selectedColumn);

        queryColumn.setSelected(true);
        QueryTable queryTable = queryColumn.getOwner();

        /*for Acion.executeUndo (RemoveQueryColumn)*/
        paramMap.put(CommandParamKey.COLUMN_ID, columnId);

        /*Action Result*/

        // save Column in Query data
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = mapper.toProjectUser(project);
        int queryId = query.getId();
        String childId = String.valueOf(queryId);
        dataManager.addData(ProjectFileType.QUERY, mapper.map(query), projectUser, queryId, step.getId(), childId);

        // save Column in Owner Table data
        dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(queryTable), projectUser, queryTable.getId(), step.getId(), childId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

    private boolean exists(String columnName, List<QueryColumn> columnList) {
        columnName = columnName.toUpperCase();
        for (QueryColumn column : columnList) {
            if (column.getName().toUpperCase().equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

}
