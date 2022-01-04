package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;

import java.util.Map;

/**
 * Add DataSource to TOWER and DataSource List.
 */
public class AddDataSource extends Command {

    public void execute(Map<CommandParamKey, Object> paramMap) {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Tower tower = (Tower) paramMap.get(CommandParamKey.TOWER);

        int id = project.newUniqueId();
        dataSource.setId(id);

        Floor floor = tower.getAvailableFloor(false);
        floor.setRoom(0, dataSource);

        switch (dataSource.getType()) {
            case DATABASE:
                project.getDatabaseMap().put(id, (Database) dataSource);
                break;

            case SFTP:
                project.getSftpMap().put(id, (SFTP) dataSource);
                break;

            case LOCAL:
                /*nothing to do for local*/
                break;
        }
    }

}
