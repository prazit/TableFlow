package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.DataTableUtil;

import java.util.List;
import java.util.Map;

/**
 * Add DataSourceSelector to TOWER and DataSourceSelector List.
 */
public class AddDataSourceSelector extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataSourceSelector dataSourceSelector = (DataSourceSelector) paramMap.get(CommandParamKey.DATA_SOURCE_SELECTOR);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Tower tower = step.getDataTower();
        Project project = step.getOwner();

        @SuppressWarnings("unchecked")
        List<DataSourceSelector> dataSourceSelectorList = (List<DataSourceSelector>) paramMap.get(CommandParamKey.DATA_SOURCE_SELECTOR_LIST);
        boolean isExecute = (dataSourceSelectorList == null);

        Floor floor;
        int roomIndex;
        int id;
        if (isExecute) {
            floor = tower.getAvailableFloor(0, false);
            roomIndex = 0;
            id = DataTableUtil.newUniqueId(project);
            dataSourceSelector.setId(id);
        } else {
            floor = dataSourceSelector.getFloor();
            roomIndex = dataSourceSelector.getRoomIndex();
            id = dataSourceSelector.getId();
        }

        /*Undo action will put dataSourceSelector at old room*/
        floor.setRoom(roomIndex, dataSourceSelector);

        /*Add to SelectorList*/
        dataSourceSelectorList = step.getDataSourceSelectorList();
        dataSourceSelectorList.add(dataSourceSelector);

        step.getSelectableMap().put(((Selectable) dataSourceSelector).getSelectableId(), dataSourceSelector);

        /*for Action.executeUndo()*/

        /*Action Result*/

        // save DataSourceSelector data
        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;
        int stepId = step.getId();
        projectDataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map(dataSourceSelector), project, dataSourceSelector.getId(), stepId );

        // save DataSource list
        projectDataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, mapper.fromDataSourceSelectorList(dataSourceSelectorList), project, 1, stepId);

        // no line to save here

        // save Tower data
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());
    }

}
