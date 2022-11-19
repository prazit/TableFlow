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
        boolean selectAll = (Boolean) paramMap.get(CommandParamKey.SWITCH_ON);
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        int removeId = (Integer) paramMap.get(CommandParamKey.COLUMN_ID);
        List<QueryColumn> columnList = query.getColumnList();

        QueryTable queryTable;
        QueryColumn removeColumn = null;
        if (selectAll) {
            /*find table by removeId and then*/
            queryTable = findTable(removeId, query.getTableList());
            if (queryTable == null) throw new UnsupportedOperationException("table not found for table-id(" + removeId + ") in query '" + query.getName() + "'");

            for (QueryColumn originalColumn : queryTable.getColumnList()) {
                if(!originalColumn.isSelected()) continue;

                /*find selected-column by originalColumnId then remove immediately*/
                removeColumn(originalColumn.getId(), columnList);

                /*mark selected on original column (in table)*/
                originalColumn.setSelected(false);
            }
            removeColumn = queryTable.getColumnList().get(0);

        } else {
            /*find selected-column by removeId and then remove immediately*/
            removeColumn = removeColumn(removeId, columnList);
            if (removeColumn == null) throw new UnsupportedOperationException("column not found for column-id(" + removeId + ") in query '" + query.getName() + "'");

            /*mark selected on original column (in table)*/
            queryTable = removeColumn.getOwner();
            for (QueryColumn originalColumn : queryTable.getColumnList()) {
                if (originalColumn.getId() == removeId) {
                    originalColumn.setSelected(false);
                    break;
                }
            }
        }

        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();

        /*for Acion.executeUndo (AddQueryColumn)*/
        paramMap.put(CommandParamKey.QUERY_COLUMN, removeColumn);

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

    private QueryColumn removeColumn(int removeId, List<QueryColumn> columnList) {
        QueryColumn column = null;
        for (int index = 0; index < columnList.size(); index++) {
            column = columnList.get(index);
            if (column.getId() == removeId) {
                columnList.remove(index);
            }
        }
        return column;
    }

    private QueryTable findTable(int id, List<QueryTable> tableList) {
        for (QueryTable table : tableList) {
            if (table.getId() == id) {
                return table;
            }
        }
        return null;
    }

}
