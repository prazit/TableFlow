package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.room.EmptyRoom;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import org.jboss.weld.manager.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Remove TransformTable only.<br/>
 * Notice: the TransformTable need to remove all related objects before.
 */
public class RemoveTransformTable extends Command {
    private static final long serialVersionUID = 2022031309996660007L;

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
        ProjectDataManager.addData(ProjectFileType.TRANSFORM_TABLE, null, project, transformTable.getId(), step.getId(), 0, transformTable.getId());

        // save TransformTable list
        ProjectDataManager.addData(ProjectFileType.TRANSFORM_TABLE_LIST, transformList, step.getOwner(), 1, step.getId());

        // save Line data
        for (Line line : removedLineList) {
            ProjectDataManager.addData(ProjectFileType.LINE, null, project, line.getId(), step.getId());
        }

        // save Object(DataTable) at the startPlug of removedLine.
        for (DataTable table : updatedTableList) {
            if (table instanceof TransformTable) {
                ProjectDataManager.addData(ProjectFileType.TRANSFORM_TABLE, table, project, table.getId(), step.getId(), 0, table.getId());
            } else {
                ProjectDataManager.addData(ProjectFileType.DATA_TABLE, table, project, table.getId(), step.getId(), table.getId());
            }
        }
        
        // save Line list
        ProjectDataManager.addData(ProjectFileType.LINE_LIST, step.getLineList(), project, 1, step.getId());

        // save Tower data
        Tower tower = floor.getTower();
        ProjectDataManager.addData(ProjectFileType.TOWER, tower, project, tower.getId(), step.getId());

        // save Floor data
        ProjectDataManager.addData(ProjectFileType.FLOOR, null, project, floor.getId(), step.getId());

    }

}
