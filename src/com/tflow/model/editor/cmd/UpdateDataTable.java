package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDataTable extends Command {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {

        /*Notice: if the DataFile already extracted, all child tables need to update(add/remove columns)*/
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        Project project = workspace.getProject();
        Step step = project.getActiveStep();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        LinePlug startPlug = dataFile.getStartPlug();

        /*collect all ExtractedTables to dataTableList*/
        List<DataTable> dataTableList = new ArrayList<>();
        for (Line line : startPlug.getLineList()) {
            Selectable selectable = selectableMap.get(line.getEndSelectableId());
            if (selectable != null) {
                dataTableList.add((DataTable) selectable);
            }
        }

        /*no extracted table then exit immediately*/
        if (dataTableList.size() == 0) {
            log.debug("exit UpdateDataTable command by no extracted data-table");
            return;
        }

        /*extract to new DataTable called updatedTable for comparing*/
        paramMap.put(CommandParamKey.STEP, step);
        DataTable updatedTable = extractData(dataFile.getType(), paramMap);

        /*compare to create remove/add/change lists*/
        List<Integer> removeList = new ArrayList<>();
        List<DataColumn> addList = new ArrayList<>();
        List<DataColumn> changeList = new ArrayList<>();
        compareTable(dataTableList.get(0), updatedTable, addList, changeList, removeList);

        boolean remove = removeList.size() > 0;
        boolean add = addList.size() > 0;
        boolean change = changeList.size() > 0;
        if (!add && !remove && !change) {
            log.debug("exit UpdateDataTable command without changed table");
            return;
        }

        /*make changes to all extracted tables*/
        List<DataColumn> removedList = new ArrayList<>();
        List<DataColumn> changedList = new ArrayList<>();
        List<DataColumn> columnList;
        DataColumn copiedColumn;
        int columnIndex;
        int number = 1;
        boolean needNumber = dataTableList.size() > 1;
        for (DataTable dataTable : dataTableList) {
            dataTable.setName(updatedTable.getName() + (needNumber ? number++ : ""));

            columnList = dataTable.getColumnList();

            if (remove) for (int rIndex = removeList.size() - 1; rIndex >= 0; rIndex--) {
                columnIndex = removeList.get(rIndex);
                copiedColumn = columnList.get(columnIndex);
                columnList.remove(columnIndex);
                removedList.add(copiedColumn);
            }

            if (add) for (DataColumn addedColumn : addList) {
                copiedColumn = new DataColumn(addedColumn.getIndex(), addedColumn.getType(), addedColumn.getName(), ProjectUtil.newElementId(project), dataTable);
                copiedColumn.setId(ProjectUtil.newUniqueId(project));
                columnList.add(copiedColumn);
                changedList.add(copiedColumn);
            }

            if (change) for (DataColumn changedColumn : changeList) {
                copiedColumn = columnList.get(changedColumn.getIndex());
                copiedColumn.setName(changedColumn.getName());
                copiedColumn.setType(changedColumn.getType());
                changedList.add(copiedColumn);
            }

        }

        if (remove || change) {
        /* TODO: future feature: case of changes of  need to
            choice1: fire event COLUMN_REMOVED to notify related TransformTable
            choice2: update all used of the column (in dynamic-value-expression)
        */
        }

        // for Action.executeUndo

        // result map
        action.getResultMap().put(ActionResultKey.SELECTABLE_LIST, dataTableList);

        /*save all DataTable*/
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();
        int stepId = step.getId();
        for (DataTable dataTable : dataTableList) {
            dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map(dataTable), projectUser, dataTable.getId(), stepId, dataTable.getId());
            dataManager.addData(ProjectFileType.DATA_COLUMN_LIST, mapper.fromDataColumnList(dataTable.getColumnList()), projectUser, dataTable.getId(), stepId, dataTable.getId());
        }

        /*save removed columns*/
        for (DataColumn dataColumn : removedList) {
            dataManager.addData(ProjectFileType.DATA_COLUMN, (TWData) null, projectUser, dataColumn.getId(), stepId, dataColumn.getOwner().getId());
        }

        /*save added and changed columns*/
        for (DataColumn dataColumn : changedList) {
            dataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), projectUser, dataColumn.getId(), stepId, dataColumn.getOwner().getId());
        }

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }

    /**
     * Replace full column list
     *
     * @param addList    will contains new column from updatedTable
     * @param changeList will contains name-changed column from updatedTable
     * @param removeList will contains column-index from dataTable
     */
    private void compareTable(DataTable dataTable, DataTable updatedTable, List<DataColumn> addList, List<DataColumn> changeList, List<Integer> removeList) {
        List<DataColumn> dataColumnList = dataTable.getColumnList();
        List<DataColumn> updatedColumnList = updatedTable.getColumnList();
        int dataSize = dataColumnList.size();
        int updatedSize = updatedColumnList.size();
        int maxSize = Math.max(dataSize, updatedSize);
        DataColumn dataColumn;
        DataColumn updatedColumn;
        String dataName;
        String updatedName;

        /** test case 1: changed
         * updated | data
         *       A | 1
         *       1 | 2
         *       2 | 3
         * (A,1=change) (1,2=change) (2,3=change)
         */

        /** test case 2: removed
         * updated | data
         *       A | 1
         *       1 | 2
         *         | 3
         * (A,1=change) (1,2=change) (null,3=remove)
         */

        /** test case 3: added
         * updated | data
         *       A | 1
         *       1 | 2
         *       2 |
         * (A,1=change) (1,2=change) (2,null=add)
         */

        for (int index = 0; index < maxSize; index++) {
            if (index >= dataSize) {
                updatedColumn = updatedColumnList.get(index);
                addList.add(updatedColumn);
            } else if (index >= updatedSize) {
                removeList.add(index);
            } else {
                updatedColumn = updatedColumnList.get(index);
                dataColumn = dataColumnList.get(index);
                dataName = dataColumn.getName();
                updatedName = updatedColumn.getName();
                if (!dataName.equalsIgnoreCase(updatedName)) {
                    /*Notice: need to change index started at 1 to started at 0*/
                    updatedColumn.setIndex(index);
                    changeList.add(updatedColumn);
                }
            }
        }
    }
}
