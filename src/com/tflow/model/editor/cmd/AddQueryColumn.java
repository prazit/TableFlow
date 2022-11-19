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
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        boolean selectAll = (Boolean) paramMap.get(CommandParamKey.SWITCH_ON);
        QueryColumn queryColumn = (QueryColumn) paramMap.get(CommandParamKey.QUERY_COLUMN);

        Project project = step.getOwner();
        QueryTable queryTable = queryColumn.getOwner();
        List<QueryColumn> columnList = query.getColumnList();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

        int removeId;
        if (selectAll) {
            for (QueryColumn column : queryTable.getColumnList()) {
                int columnId = column.getId();
                String columnName = column.getName();
                if (exists(columnName, columnList)) continue;
                column.setSelected(true);
                addColumnTo(columnList, column, columnId, columnName);
            }
            removeId = queryTable.getId();

        } else {
            int columnId = queryColumn.getId();
            String columnName = queryColumn.getName();
            if (exists(columnName, columnList)) {
                throw new UnsupportedOperationException("Column name '" + columnName + "' already exists");
            }
            queryColumn.setSelected(true);
            addColumnTo(columnList, queryColumn, columnId, columnName);
            removeId = columnId;
        }

        /*for Acion.executeUndo (RemoveQueryColumn)*/
        paramMap.put(CommandParamKey.COLUMN_ID, removeId);

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

    private void addColumnTo(List<QueryColumn> columnList, QueryColumn prototypeColumn, int id, String name) {
        QueryColumn selectedColumn = new QueryColumn();
        selectedColumn.setId(id);
        selectedColumn.setIndex(columnList.size());
        selectedColumn.setSelected(true);
        selectedColumn.setType(ColumnType.NORMAL);
        selectedColumn.setName(name);
        selectedColumn.setValue(prototypeColumn.getValue());
        selectedColumn.setOwner(prototypeColumn.getOwner());
        columnList.add(selectedColumn);
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
