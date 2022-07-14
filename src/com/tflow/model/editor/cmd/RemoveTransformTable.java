package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import org.jboss.weld.manager.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Remove TransformTable only.<br/>
 * Notice: the TransformTable need to remove all related objects before.
 */
public class RemoveTransformTable extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        TransformTable transformTable = (TransformTable) paramMap.get(CommandParamKey.TRANSFORM_TABLE);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*remove line between sourceTables and transformTable (support future feature, merge table) */
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        LinePlug endPlug = transformTable.getEndPlug();
        List<Line> removedLineList = new ArrayList<>(endPlug.getLineList());
        List<DataTable> updatedTableList = new ArrayList<>();
        for (Line line : removedLineList) {
            updatedTableList.add((DataTable) selectableMap.get(line.getStartSelectableId()));
        }
        step.removeLine(endPlug);

        /*remove from Tower*/
        Project project = step.getOwner();
        Floor floor = transformTable.getFloor();
        int roomIndex = transformTable.getRoomIndex();
        floor.setRoom(roomIndex, new EmptyRoom(roomIndex, floor, project.newElementId()));

        /*remove from TransformTable List*/
        List<TransformTable> transformList = step.getTransformList();
        transformList.remove(transformTable);

        /*remove from selectableMap*/
        selectableMap.remove(transformTable.getSelectableId());

        /*for Action.executeUndo()*/
        DataTable dataTable = (DataTable) selectableMap.get(transformTable.getSourceSelectableId());
        paramMap.put(CommandParamKey.TRANSFORM_TABLE, transformTable);
        paramMap.put(CommandParamKey.DATA_TABLE, dataTable);

        // save TransformTable data
        ProjectDataManager projectDataManager = project.getManager();
        ProjectMapper mapper = projectDataManager.mapper;
        projectDataManager.addData(ProjectFileType.TRANSFORM_TABLE, (TWData) null, project, transformTable.getId(), step.getId(), 0, transformTable.getId());

        // save TransformTable list
        projectDataManager.addData(ProjectFileType.TRANSFORM_TABLE_LIST, mapper.fromTransformTableList(transformList), step.getOwner(), 1, step.getId());

        // save Line data
        for (Line line : removedLineList) {
            projectDataManager.addData(ProjectFileType.LINE, (TWData) null, project, line.getId(), step.getId());
        }

        // save Object(DataTable) at the startPlug of removedLine.
        for (DataTable table : updatedTableList) {
            if (table instanceof TransformTable) {
                projectDataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map((TransformTable) table), project, table.getId(), step.getId(), 0, table.getId());
            } else {
                projectDataManager.addData(ProjectFileType.DATA_TABLE, mapper.map((DataTable) table), project, table.getId(), step.getId(), table.getId());
            }
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
