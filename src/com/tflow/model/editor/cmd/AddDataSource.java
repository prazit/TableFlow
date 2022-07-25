package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataSourceData;
import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.DataTableUtil;

import java.util.List;
import java.util.Map;

/**
 * Add DataSource to TOWER and DataSource List.
 */
public class AddDataSource extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Tower tower = step.getDataTower();
        Project project = step.getOwner();

        @SuppressWarnings("unchecked")
        List<DataFile> dataFileList = (List<DataFile>) paramMap.get(CommandParamKey.DATA_FILE_LIST);
        boolean isExecute = (dataFileList == null);

        Floor floor;
        int roomIndex;
        int id;
        if (isExecute) {
            floor = tower.getAvailableFloor(0, false);
            roomIndex = 0;
            id = DataTableUtil.newUniqueId(project);
            dataSource.setId(id);
        } else {
            floor = dataSource.getFloor();
            roomIndex = dataSource.getRoomIndex();
            id = dataSource.getId();
        }

        /*Undo action will put dataSource at old room*/
        floor.setRoom(roomIndex, dataSource);

        ProjectDataManager projectDataManager = project.getDataManager();
        ProjectMapper mapper = projectDataManager.mapper;
        ProjectFileType fileType;
        ProjectFileType listFileType;
        List<Integer> idList;
        DataSourceData dataSourceData;
        switch (dataSource.getType()) {
            case DATABASE:
                project.getDatabaseMap().put(id, (Database) dataSource);
                fileType = ProjectFileType.DB;
                listFileType = ProjectFileType.DB_LIST;
                dataSourceData = mapper.map((Database) dataSource);
                idList = mapper.fromMap(project.getDatabaseMap());
                break;

            case SFTP:
                project.getSftpMap().put(id, (SFTP) dataSource);
                fileType = ProjectFileType.SFTP;
                listFileType = ProjectFileType.SFTP_LIST;
                dataSourceData = mapper.map((SFTP) dataSource);
                idList = mapper.fromMap(project.getSftpMap());
                break;

            default: //case LOCAL:
                project.getLocalMap().put(id, (Local) dataSource);
                fileType = ProjectFileType.LOCAL;
                listFileType = ProjectFileType.LOCAL_LIST;
                dataSourceData = mapper.map((Local) dataSource);
                idList = mapper.fromMap(project.getLocalMap());
        }

        Selectable selectable = (Selectable) dataSource;
        step.getSelectableMap().put(selectable.getSelectableId(), selectable);

        /*for Acion.executeUndo*/

        /*Action Result*/

        // save DataSource data
        projectDataManager.addData(fileType, dataSourceData, project, dataSource.getId());

        // save DataSource list
        projectDataManager.addData(listFileType, idList, project);

        // no line to save here

        // save Tower data
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), step.getId());

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        projectDataManager.addData(ProjectFileType.PROJECT, mapper.map(project), project, project.getId());

    }

}
