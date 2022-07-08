package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
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

        /*remove from selectableMap*/
        selectableMap.remove(((Selectable) dataSource).getSelectableId());

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_SOURCE, dataSource);
        paramMap.put(CommandParamKey.DATA_FILE_LIST, dataFileList);

        // no DataSource to save here // Notice: don't remove data-source from project because of data-sources are shared between steps, go to Project page to manage all data-sources

        // save Line data
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        for (Line line : lineList) {
            projectDataManager.addData(ProjectFileType.LINE, (TWData) null, project, line.getId(), step.getId());
        }

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), project, 1, step.getId());

        // save Tower data
        Tower tower = floor.getTower();
        projectDataManager.addData(ProjectFileType.TOWER, mapper.map(tower), project, tower.getId(), step.getId());

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, (TWData) null, project, floor.getId(), step.getId());
    }

}
