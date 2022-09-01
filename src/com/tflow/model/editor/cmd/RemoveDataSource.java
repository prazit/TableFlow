package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataSourceData;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class RemoveDataSource extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        /*remove from Tower*/
        Floor floor = dataSource.getFloor();
        int roomIndex = dataSource.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, ProjectUtil.newElementId(project)));

        /*remove from selectableMap*/
        selectableMap.remove(((Selectable) dataSource).getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_SOURCE, dataSource);

        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        DataSourceData dataSourceData;
        ProjectFileType fileType;
        ProjectFileType listFileType;
        List<Integer> idList;
        switch (dataSource.getType()) {
            case DATABASE:
                fileType = ProjectFileType.DB;
                listFileType = ProjectFileType.DB_LIST;
                dataSourceData = mapper.map((Database) dataSource);
                idList = mapper.fromMap(project.getDatabaseMap());
                break;
            case SFTP:
                fileType = ProjectFileType.SFTP;
                listFileType = ProjectFileType.SFTP_LIST;
                dataSourceData = mapper.map((SFTP) dataSource);
                idList = mapper.fromMap(project.getSftpMap());
                break;
            case LOCAL:
                fileType = ProjectFileType.LOCAL;
                listFileType = ProjectFileType.LOCAL_LIST;
                dataSourceData = mapper.map((Local) dataSource);
                idList = mapper.fromMap(project.getLocalMap());
                break;
            default: //case SYSTEM:
                fileType = ProjectFileType.DS;
                listFileType = ProjectFileType.DS_LIST;
                dataSourceData = null;
                idList = null;
        }

        if (idList != null) {
            // save DataSource data
            dataManager.addData(fileType, dataSourceData, projectUser, dataSource.getId());

            // save DataSource list
            dataManager.addData(listFileType, idList, projectUser);
        }

        // save Tower data
        Tower tower = floor.getTower();
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), step.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
