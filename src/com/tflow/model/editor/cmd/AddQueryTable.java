package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.query.TableJoinType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AddQueryTable extends QueryCommand {

    private Logger log = LoggerFactory.getLogger(AddQueryTable.class);

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

        /*add to selectableMap*/
        step.getSelectableMap().put(queryTable.getSelectableId(), queryTable);

        /*the table need columns*/
        loadColumnList(queryTable, query.getOwner(), project, workspace);
        markSelectedColumn(queryTable, query.getColumnList());

        /*the table need more information for Joined table*/
        List<QueryTable> fkTableList = collectFKTable(queryTable.getSchema(), queryTable.getName(), tableList);
        boolean hasFkTable = fkTableList.size() > 0;
        if (hasFkTable) {
            QueryTable fkTable = null;
            String joinCondition = null;
            for (QueryTable table : fkTableList) {
                joinCondition = getJoinCondition(queryTable, table);
                if (joinCondition != null) {
                    fkTable = table;
                    break;
                }
            }

            if (fkTable == null) {
                hasFkTable = false;
            } else {
                queryTable.setJoinType(TableJoinType.LEFT_JOIN);
                queryTable.setJoinTable(fkTable.getName());
                queryTable.setJoinTableId(fkTable.getId());
                queryTable.setJoinCondition(joinCondition);

                /*need line between (pk)queryTable and fkTable*/
                setStep(step);
                addLine(queryTable.getSelectableId(), fkTable.getSelectableId(), query.getLineList());
            }
        }

        /*add no-join-table to tower*/
        if (!hasFkTable) {
            Tower tower = query.getTower();
            int roomIndex = tower.getRoomsOnAFloor();
            tower.addRoom(1);
            tower.setRoom(0, roomIndex, queryTable);
        }

        /*TODO: need to find existing tables that can join to this table*/

        /*for next Command (RearrangeQueryTable)*/
        paramMap.put(CommandParamKey.SWITCH_ON, hasFkTable);

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

    private String getJoinCondition(QueryTable pkTable, QueryTable fkTable) {
        StringBuilder condition = new StringBuilder();

        String pkTableName = pkTable.getName();
        String fkTableName = fkTable.getName();

        Queue<QueryColumn> pkColumnList = new PriorityQueue<>(pkTable.getColumnList().stream().filter(QueryColumn::isPk).collect(Collectors.toList()));
        List<QueryColumn> fkColumnList = fkTable.getColumnList().stream().filter(QueryColumn::isFk).collect(Collectors.toList());
        if (pkColumnList.size() != fkColumnList.size()) {
            if (log.isDebugEnabled()) log.debug("getJoinCondition(pkTable:{}, fkTable:{}): fk-column count({}) not match pk-column count({}).", pkTableName, fkTableName, pkColumnList.size(), fkColumnList.size());
            return null;
        }

        QueryColumn matchColumn;
        QueryColumn pkColumn;
        int deadLoopDectection = pkColumnList.size() * fkColumnList.size();
        int loopCount = 0;
        while (pkColumnList.size() > 0 && loopCount < deadLoopDectection) {
            loopCount++;
            pkColumn = pkColumnList.poll();

            /*try to match column by name*/
            matchColumn = null;
            for (QueryColumn fkColumn : fkColumnList) {
                if (fkColumn.getName().equalsIgnoreCase(pkColumn.getName())) {
                    matchColumn = fkColumn;
                    break;
                }
            }

            /*try to match next columns before*/
            if (matchColumn == null) {
                pkColumnList.add(pkColumn);
                continue;
            }
            fkColumnList.remove(matchColumn);

            /*condition for match column*/
            if (condition.length() > 0) condition.append(" and ");
            condition
                    .append(pkTableName).append(".").append(pkColumn.getName())
                    .append(" = ")
                    .append(fkTableName).append(".").append(matchColumn.getName());
        }

        if (pkColumnList.size() > 0) {
            for (int index = 0; index < pkColumnList.size(); index++) {
                pkColumn = pkColumnList.poll();
                matchColumn = fkColumnList.remove(0);

                /*try to match by index*/
                if (condition.length() > 0) condition.append(" and ");
                condition
                        .append(pkTableName).append(".").append(pkColumn.getName())
                        .append(" = ")
                        .append(fkTableName).append(".").append(matchColumn.getName());
            }
        }

        /* pkTable.pkColumn1 = fkTable.fkColumn1
            and pkTable.pkColumn2 = fkTable.fkColumn2 */
        return condition.toString();
    }

    private List<QueryTable> collectFKTable(String schema, String tableName, List<QueryTable> tableList) {
        List<QueryTable> fkTableList = new ArrayList<>();
        for (QueryTable table : tableList) {
            for (QueryColumn column : table.getColumnList()) {
                if (column.isFk() && column.getFkSchema().equalsIgnoreCase(schema) && column.getFkTable().equalsIgnoreCase(tableName)) {
                    fkTableList.add(table);
                }
            }
        }
        return fkTableList;
    }

}
