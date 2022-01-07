package com.tflow.model.editor.cmd;

import com.tflow.model.editor.DataFile;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.LineType;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

/**
 * Add DataSource to TOWER and wait for Extraction Command.
 */
public class AddDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        Tower tower = (Tower) paramMap.get(CommandParamKey.TOWER);
        List<Line> lineList = (List<Line>) paramMap.get(CommandParamKey.LINE_LIST);

        Floor floor = tower.getAvailableFloor(1,false);
        floor.setRoom(1, dataFile);

        /*check Room1 on every floor to find duplicated DataSource, mark for suppress and redirect line to the existing DataSource*/
        List<Room> room0List = tower.getStack(0);
        DataSource dataSource = dataFile.getDataSource();
        DataSource foundDataSource = null;
        String dataSourcePlug = dataSource.getPlug();
        for (Room room0 : room0List) {
            if (room0 instanceof DataSource) {
                DataSource dataSource0 = (DataSource) room0;
                if (dataSource0.equals(dataSource)) {
                    foundDataSource = dataSource0;
                    dataSourcePlug = dataSource0.getPlug();
                }
            }
        }

        lineList.add(new Line(dataSourcePlug, dataFile.getEndPlug(), LineType.TABLE));

    }

}
