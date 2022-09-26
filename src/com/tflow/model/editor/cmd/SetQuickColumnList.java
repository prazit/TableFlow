package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SetQuickColumnList extends Command {

    private Logger log = LoggerFactory.getLogger(getClass());

    private enum Operation {
        NOOP,
        UPDATE,
        APPEND,
        REMOVE;
    }

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        List<NameValue> quickColumnList = transformTable.getQuickColumnList();
        Step step = transformTable.getOwner();
        Project project = step.getOwner();
        log.debug("SetQuickColumnList: {}", quickColumnList.toArray());

        List<DataColumn> columnList = transformTable.getColumnList();
        TransformColumn transformColumn = null;
        Operation operation = Operation.NOOP;

        int columnCount = columnList.size();
        if (columnCount > quickColumnList.size()) {
            // remove last column
            operation = Operation.REMOVE;
            transformColumn = (TransformColumn) columnList.remove(columnList.size() - 1);
            step.getSelectableMap().remove(transformColumn.getSelectableId());
        }

        /*Notice: only one column has changed at a time*/
        else for (int index = 0; index < quickColumnList.size(); index++) {
            NameValue nameValue = quickColumnList.get(index);

            if (index < columnCount) {
                // update existing column if changed
                transformColumn = (TransformColumn) columnList.get(index);
                String name = transformColumn.getName() == null ? "" : transformColumn.getName();
                String value = transformColumn.getValue() == null ? "" : transformColumn.getValue();
                if (!name.equals(nameValue.getName()) || !value.equals(nameValue.getValue())) {
                    operation = Operation.UPDATE;
                    transformColumn.setName(nameValue.getName());
                    nameValue.setValue(transformColumn.setValue(nameValue.getValue()));
                    break;
                }

            } else {
                // append new column
                operation = Operation.APPEND;
                transformColumn = new TransformColumn(columnCount, DataType.STRING, nameValue.getName(), ProjectUtil.newElementId(project), ProjectUtil.newElementId(project), transformTable);
                transformColumn.setId(ProjectUtil.newUniqueId(project));
                nameValue.setValue(transformColumn.setValue(nameValue.getValue()));
                columnList.add(transformColumn);
                step.getSelectableMap().put(transformColumn.getSelectableId(), transformColumn);
                break;
            }
        }

        if (transformColumn == null) {
            UnsupportedOperationException ex = new UnsupportedOperationException("Unexpected error occurred! no column are changed");
            log.error(ex.getMessage(), ex);
            throw ex;
        }

        // for Action.executeUndo /*nothing*/

        // result map /*nothing*/

        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int transformTableId = transformTable.getId();
        int stepId = step.getId();

        // save data
        if (Operation.REMOVE == operation) {
            dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, (TWData) null, projectUser, transformColumn.getId(), stepId, 0, transformTableId);
        } else {
            /*(Operation.APPEND == operation || Operation.UPDATE == operation)*/
            dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map(transformColumn), projectUser, transformColumn.getId(), stepId, 0, transformTableId);
        }

        if (Operation.APPEND == operation || Operation.REMOVE == operation) {
            dataManager.addData(ProjectFileType.TRANSFORM_COLUMN_LIST, mapper.fromDataColumnList(columnList), projectUser, transformTableId, stepId, 0, transformTableId);
            dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
        }

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }
}
