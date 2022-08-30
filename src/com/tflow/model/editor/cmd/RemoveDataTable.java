package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * Remove DataTable only.<br/>
 * Notice: the DataTable need to remove all related objects before.
 */
public class RemoveDataTable extends Command {

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
        removeLine(removedLine);

        /*remove from Tower*/
        Project project = step.getOwner();
        Floor floor = dataTable.getFloor();
        int roomIndex = dataTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, ProjectUtil.newElementId(project)));

        /*remove from DataTable List*/
        List<DataTable> dataList = step.getDataList();
        dataList.remove(dataTable);

        /* TODO: move dataFile to the fl.of first child */
        /* TODO: set dataFile.owner = first child */

        /*remove from selectableMap*/
        ProjectUtil.removeFrom(step.getSelectableMap(), dataTable);

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);
        paramMap.put(CommandParamKey.DATA_FILE, dataTable.getDataFile());

        // save DataTable data
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int dataTableId = dataTable.getId();
        dataManager.addData(ProjectFileType.DATA_TABLE, (TWData) null, projectUser, dataTableId, step.getId(), dataTableId);

        // save DataTable list
        dataManager.addData(ProjectFileType.DATA_TABLE_LIST, mapper.fromDataTableList(dataList), projectUser, dataTableId, step.getId());

        // save Line data
        dataManager.addData(ProjectFileType.LINE, (TWData) null, projectUser, removedLine.getId(), step.getId());

        // save object(DataFile) at endPlug.
        dataManager.addData(ProjectFileType.DATA_FILE, (TWData) null, projectUser, dataFile.getId(), step.getId());

        // save Line list
        dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, 1, step.getId());

        // save Tower data
        Tower tower = floor.getTower();
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), step.getId());
    }

}
