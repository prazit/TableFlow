package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoveDataSource extends Command {
    private static final long serialVersionUID = 2022031309996660014L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        /*remove remaining lines on startPlug*/
        List<DataFile> dataFileList = new ArrayList<>();
        List<Line> lineList = new ArrayList<>(dataSource.getPlug().getLineList());
        for (Line line : lineList) {
            /*need to remove dataSource from dataFile at the end of line*/
            DataFile dataFile = (DataFile) selectableMap.get(line.getEndSelectableId());
            step.removeLine(line);
            dataFile.setDataSource(null);
            dataFileList.add(dataFile);
        }

        /*remove from Tower*/
        Floor floor = dataSource.getFloor();
        int roomIndex = dataSource.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, project.newElementId()));

        /*Notice: don't remove data-source from project, go to Project page to manage all data-source*/

        /*remove from selectableMap*/
        selectableMap.remove(((Selectable) dataSource).getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_SOURCE, dataSource);
        paramMap.put(CommandParamKey.DATA_FILE_LIST, dataFileList);
    }

}
