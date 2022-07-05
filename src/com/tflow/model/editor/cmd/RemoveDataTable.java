package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.util.DataTableUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Remove DataTable only.<br/>
 * Notice: the DataTable need to remove all related objects before.
 */
public class RemoveDataTable extends Command {
    private static final long serialVersionUID = 2022031309996660011L;

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataTable dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*TODO: need to create actions to remove all child at first, history will change between this process (keep no chain no effect)*/

        /*remove line between dataFile and dataTable*/
        LinePlug endPlug = dataTable.getEndPlug();
        Line removedLine = endPlug.getLine();
        DataFile dataFile = (DataFile) step.getSelectableMap().get(removedLine.getStartSelectableId());
        step.removeLine(removedLine);

        /*remove from Tower*/
        Project project = step.getOwner();
        Floor floor = dataTable.getFloor();
        int roomIndex = dataTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, project.newElementId()));

        /*remove from DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataList.remove(dataTable);

        /*remove from selectableMap*/
        DataTableUtil.removeFrom(step.getSelectableMap(), dataTable);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.DATA_FILE, dataTable.getDataFile());

        // save DataTable data
        ProjectDataManager projectDataManager = project.getManager();
        int dataTableId = dataTable.getId();
        projectDataManager.addData(ProjectFileType.DATA_TABLE, null, project, dataTableId, step.getId(), dataTableId);

        // save DataTable list
        projectDataManager.addData(ProjectFileType.DATA_TABLE_LIST, dataList, project, dataTableId, step.getId());

        // save Line data
        projectDataManager.addData(ProjectFileType.LINE, null, project, removedLine.getId(), step.getId());

        // save object(DataFile) at endPlug.
        projectDataManager.addData(ProjectFileType.DATA_FILE, null, project, dataFile.getId(), step.getId());

        // save Line list
        projectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, 1, step.getId());

        // save Tower data
        Tower tower = floor.getTower();
        projectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        projectDataManager.addData(ProjectFileType.FLOOR, floor, project, floor.getId(), step.getId());
    }

}
