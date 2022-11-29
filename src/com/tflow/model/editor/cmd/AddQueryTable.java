package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.query.ColumnType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.editor.sql.Query;
import com.tflow.model.editor.sql.QueryColumn;
import com.tflow.model.editor.sql.QueryTable;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class AddQueryTable extends QueryCommand {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        Query query = (Query) paramMap.get(CommandParamKey.QUERY);
        QueryTable queryTable = (QueryTable) paramMap.get(CommandParamKey.QUERY_TABLE);

        Project project = workspace.getProject();
        Step step = project.getActiveStep();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

        /*turn temporary to selected-table*/
        queryTable.setId(ProjectUtil.newUniqueId(project));

        /*add table to query*/
        List<QueryTable> tableList = query.getTableList();
        tableList.add(queryTable);

        /*add table to tower*/
        Tower tower = query.getTower();
        int roomIndex = tower.getRoomsOnAFloor();
        tower.addRoom(1);
        tower.setRoom(0, roomIndex, queryTable);

        /*need columns*/
        loadColumnList(queryTable, query.getOwner(), project, workspace);
        markSelectedColumn(queryTable, query.getColumnList());

        /*TODO: need more info about Joined Table
            TableJoinType joinType;
            String joinTable;
            int joinTableId;
            String joinCondition;
        */

        /*for Acion.executeUndo (RemoveQueryTable)*/
        //paramMap.put(CommandParamKey.QUERY_TABLE, queryTable);

        /*Action Result*/

        // save Query data
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        int queryId = query.getId();
        String childId = String.valueOf(queryId);
        dataManager.addData(ProjectFileType.QUERY_TABLE_LIST, mapper.fromQueryTableList(tableList), projectUser, queryId, stepId, childId);

        // save Table data
        dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(queryTable), projectUser, queryTable.getId(), step.getId(), childId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
