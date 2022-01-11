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
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Tower tower = step.getDataTower();
        Project project = step.getOwner();

        int id = project.newUniqueId();
        dataSource.setId(id);

        Floor floor = tower.getAvailableFloor(0, false);
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

        Selectable selectable = (Selectable) dataSource;
        step.getSelectableMap().put(selectable.getSelectableId(), selectable);
    }

}
