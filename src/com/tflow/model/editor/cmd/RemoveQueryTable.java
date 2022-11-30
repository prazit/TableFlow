package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveQueryTable extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        QueryTable queryTable = (QueryTable) paramMap.get(CommandParamKey.QUERY_TABLE);

        Project project = workspace.getProject();
        Step step = project.getActiveStep();

        /*remove selected-table from query*/
        List<QueryTable> tableList = query.getTableList();
        tableList.remove(queryTable);

        /*remove selected-columns like unselect all columns from the table*/
        List<QueryColumn> columnList = query.getColumnList();
        QueryColumn removeColumn = null;
        for (QueryColumn originalColumn : queryTable.getColumnList()) {
            if (!originalColumn.isSelected()) continue;

            /*find selected-column by originalColumnId then remove immediately*/
            removeColumn(originalColumn.getId(), columnList);

            /*mark selected on original column (in table)*/
            originalColumn.setSelected(false);
        }

        /*need to remove columns from queryTable to turn it back to the state before AddQueryTable*/
        queryTable.setColumnList(new ArrayList<>());

        /*remove from Tower*/
        Tower tower = query.getTower();
        tower.setEmptyRoom(queryTable.getFloorIndex(), queryTable.getRoomIndex());

        /*for Acion.executeUndo (AddQueryTable)*/
        //paramMap.put(CommandParamKey.QUERY_TABLE, queryTable);

        /*Action Result*/

        // save removed Column in Query data
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = mapper.toProjectUser(project);
        int queryId = query.getId();
        String childId = "" + queryId;
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.QUERY, mapper.map(query), projectUser, queryId, stepId, childId);
        dataManager.addData(ProjectFileType.QUERY_TABLE_LIST, mapper.fromQueryTableList(tableList), projectUser, queryId, stepId, childId);

        // save Table data
        dataManager.addData(ProjectFileType.QUERY_TABLE, (TWData) null, projectUser, queryTable.getId(), stepId, childId);

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

}
