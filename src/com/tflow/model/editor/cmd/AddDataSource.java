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

import java.util.List;
import java.util.Map;

/**
 * Add DataSource to TOWER and DataSource List.
 */
public class AddDataSource extends Command {
    private static final long serialVersionUID = 2022031309996660000L;

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
            id = project.newUniqueId();
            dataSource.setId(id);
        } else {
            floor = dataSource.getFloor();
            roomIndex = dataSource.getRoomIndex();
            id = dataSource.getId();
        }

        /*Undo action will put dataSource at old room*/
        floor.setRoom(roomIndex, dataSource);

        ProjectDataManager projectDataManager = project.getManager();
        ProjectFileType fileType;
        ProjectFileType listFileType;
        List<Integer> idList;
        DataSourceData dataSourceData;
        switch (dataSource.getType()) {
            case DATABASE:
                project.getDatabaseMap().put(id, (Database) dataSource);
                fileType = ProjectFileType.DB;
                listFileType = ProjectFileType.DB_LIST;
                dataSourceData = projectDataManager.dataSourceMapper.map((Database) dataSource);
                idList = projectDataManager.idListMapper.map(project.getDatabaseMap());
                break;

            case SFTP:
                project.getSftpMap().put(id, (SFTP) dataSource);
                fileType = ProjectFileType.SFTP;
                listFileType = ProjectFileType.SFTP_LIST;
                dataSourceData = projectDataManager.dataSourceMapper.map((SFTP) dataSource);
                idList = projectDataManager.idListMapper.map(project.getSftpMap());
                break;

            default: //case LOCAL:
                project.getLocalMap().put(id, (Local) dataSource);
                fileType = ProjectFileType.LOCAL;
                listFileType = ProjectFileType.LOCAL_LIST;
                dataSourceData = projectDataManager.dataSourceMapper.map((Local) dataSource);
                idList = projectDataManager.idListMapper.map(project.getLocalMap());
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
        projectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
    }

}
