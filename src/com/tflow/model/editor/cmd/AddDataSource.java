package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataSourceData;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
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

        int id;
        if (isExecute) {
            id = ProjectUtil.newUniqueId(project);
            dataSource.setId(id);
        } else {
            id = dataSource.getId();
        }

        ProjectManager manager = project.getManager();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
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
                manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getDatabaseMap().values()));
                break;

            case SFTP:
                project.getSftpMap().put(id, (SFTP) dataSource);
                fileType = ProjectFileType.SFTP;
                listFileType = ProjectFileType.SFTP_LIST;
                dataSourceData = mapper.map((SFTP) dataSource);
                idList = mapper.fromMap(project.getSftpMap());
                manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getSftpMap().values()));
                break;

            default: //case LOCAL:
                project.getLocalMap().put(id, (Local) dataSource);
                fileType = ProjectFileType.LOCAL;
                listFileType = ProjectFileType.LOCAL_LIST;
                dataSourceData = mapper.map((Local) dataSource);
                idList = mapper.fromMap(project.getLocalMap());
                manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getLocalMap().values()));
        }

        Selectable selectable = (Selectable) dataSource;
        step.getSelectableMap().put(selectable.getSelectableId(), selectable);

        /*for Acion.executeUndo*/

        /*Action Result*/

        // save DataSource data
        dataManager.addData(fileType, dataSourceData, projectUser, dataSource.getId());

        // save DataSource list
        dataManager.addData(listFileType, idList, projectUser);

        // no line, no tower to save here

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }

}
