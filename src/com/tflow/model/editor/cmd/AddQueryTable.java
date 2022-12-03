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

        /*add to saveList*/
        List<QueryTable> saveList = new ArrayList<>();
        saveList.add(queryTable);

        /*the table need columns*/
        loadColumnList(queryTable, query.getOwner(), project, workspace);
        markSelectedColumn(queryTable, query.getColumnList());

        /*need Join Info for this table*/
        String tableName = queryTable.getName();
        List<QueryTable> fkTableList = collectFKTable(queryTable.getSchema(), tableName, tableList);
        boolean hasFkTable = fkTableList.size() > 0;
        setStep(step);
        if (hasFkTable) {
            QueryTable fkTable = null;
            String joinCondition = null;
            for (QueryTable table : fkTableList) {
                if (table.getName().equalsIgnoreCase(tableName)) continue;
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
                addLine(fkTable.getSelectableId(), queryTable.getSelectableId(), query.getLineList());
            }
        }

        /*need Join Info for older tables that has Join-Type == NONE*/
        String joinCondition = null;
        int tableId = queryTable.getId();
        for (QueryTable pkTable : tableList) {
            if (pkTable.getJoinType() != TableJoinType.NONE || pkTable.getName().equalsIgnoreCase(tableName)) continue;

            joinCondition = getJoinCondition(pkTable, queryTable);
            if (joinCondition != null) {
                hasFkTable = true;
                saveList.add(pkTable);

                pkTable.setJoinType(TableJoinType.LEFT_JOIN);
                pkTable.setJoinTable(tableName);
                pkTable.setJoinTableId(tableId);
                pkTable.setJoinCondition(joinCondition);

                /*need line between (pk)queryTable and fkTable*/
                addLine(queryTable.getSelectableId(), pkTable.getSelectableId(), query.getLineList());
            }
        }

        /*add no-join-table to tower*/
        /*TODO: uncomment after RearrageQueryTable exist*/
        /*if (!hasFkTable) {*/
            Tower tower = query.getTower();
            int roomIndex = tower.getRoomsOnAFloor();
            tower.addRoom(1);
            tower.setRoom(0, roomIndex, queryTable);
        /*}*/

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
        for (QueryTable saveTable : saveList) {
            dataManager.addData(ProjectFileType.QUERY_TABLE, mapper.map(saveTable), projectUser, saveTable.getId(), step.getId(), childId);
        }

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
        List<QueryColumn> fkColumnList = fkTable.getColumnList().stream().filter(fkCol -> fkCol.isFk() && fkCol.getFkTable().equalsIgnoreCase(pkTableName)).collect(Collectors.toList());
        if (pkColumnList.size() != fkColumnList.size()) {
            if (log.isDebugEnabled()) log.debug("getJoinCondition(pkTable:{}, fkTable:{}): pk-column count({}) not match fk-column count({}).", pkTableName, fkTableName, pkColumnList.size(), fkColumnList.size());
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
                if (log.isDebugEnabled()) log.debug("getJoinCondition: no column in fkTable({}) that match the pkColumn({}.{})", fkTableName, pkTableName, pkColumn.getName());
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
        String joinCondition = condition.toString();
        if (log.isDebugEnabled()) log.debug("getJoinCondition(pkTable:{}, fkTable:{}) return '{}'.", pkTableName, fkTableName, joinCondition);
        return joinCondition;
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
