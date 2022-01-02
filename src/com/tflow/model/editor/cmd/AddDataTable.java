package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

/**
 * Add DataTable to TOWER and DataTable List.
 */
public class AddDataTable extends Command {


    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Tower tower = (Tower) paramMap.get(CommandParamKey.TOWER);
        List<Line> lineList = (List<Line>) paramMap.get(CommandParamKey.LINE_LIST);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);

        DataFile dataFile = dataTable.getDataFile();
        DataSource dataSource = dataFile.getDataSource();

        /*check Room1 on every floor to find duplicated DataSource, mark for suppress and redirect line to the existing DataSource*/
        List<Room> room0List = tower.getStack(0);
        DataSource foundDataSource = null;
        String dataSourcePlug = dataSource.getPlug();
        for (Room room0 : room0List) {
            if (room0 instanceof DataSource) {
                DataSource dataSource0 = (DataSource) room0;
                if (dataSource0.getName().equals(dataSource.getName())) {
                    foundDataSource = dataSource0;
                    dataSourcePlug = dataSource0.getPlug();
                }
            }
        }

        Floor floor = tower.getAvailableFloor(false);
        if (foundDataSource == null) {
            floor.setRoom(0, dataSource);
        }
        floor.setRoom(1, dataFile);
        floor.setRoom(2, dataTable);

        lineList.add(new Line(dataSourcePlug, dataFile.getEndPlug(), LineType.TABLE));
        lineList.add(new Line(dataFile.getStartPlug(), dataTable.getEndPlug(), LineType.TABLE));

        /*Add to DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataTable.setIndex(dataList.size());
        dataList.add(dataTable);
    }

}
