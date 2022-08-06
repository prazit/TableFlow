package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

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
            id = ProjectUtil.newUniqueId(project);
            dataSourceSelector.setId(id);
        } else {
            floor = dataSourceSelector.getFloor();
            roomIndex = dataSourceSelector.getRoomIndex();
            id = dataSourceSelector.getId();
        }
        dataSourceSelector.setOwner(step);

        /*Undo action will put dataSourceSelector at old room*/
        floor.setRoom(roomIndex, dataSourceSelector);

        /*Add to SelectorList*/
        dataSourceSelectorList = step.getDataSourceSelectorList();
        dataSourceSelectorList.add(dataSourceSelector);

        step.getSelectableMap().put(((Selectable) dataSourceSelector).getSelectableId(), dataSourceSelector);

        /*for Action.executeUndo()*/

        /*Action Result*/

        // save DataSourceSelector data
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map(dataSourceSelector), projectUser, dataSourceSelector.getId(), stepId );

        // save DataSource list
        dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR_LIST, mapper.fromDataSourceSelectorList(dataSourceSelectorList), projectUser, 1, stepId);

        // no line to save here

        // save Tower data
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
    }

}
