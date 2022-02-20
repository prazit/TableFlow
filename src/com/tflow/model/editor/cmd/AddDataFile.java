package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

public class AddDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Tower tower = step.getDataTower();

        dataFile.setId(project.newUniqueId());

        Floor floor = tower.getAvailableFloor(1, false);
        floor.setRoom(1, dataFile);

        /*check Room1 on every floor to find duplicated DataSource, mark for suppress and redirect line to the existing DataSource*/
        List<Room> room0List = tower.getStack(0);
        Selectable dataSource = (Selectable) dataFile.getDataSource();
        String startSelectableId = dataSource.getSelectableId();
        for (Room room0 : room0List) {
            if (room0 instanceof DataSource) {
                DataSource dataSource0 = (DataSource) room0;
                if (dataSource0.equals(dataSource)) {
                    startSelectableId = ((Selectable) dataSource0).getSelectableId();
                }
            }
        }

        step.getSelectableMap().put(dataFile.getSelectableId(), dataFile);
        step.addLine(startSelectableId, dataFile.getSelectableId());

    }

}
