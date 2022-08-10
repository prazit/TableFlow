package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class AddDataFile extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Project project = step.getOwner();
        Tower tower = step.getDataTower();
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /*support undo of Action 'RemoveDataFile'*/
        DataFile dataFile = (DataFile) paramMap.get(CommandParamKey.DATA_FILE);
        DataTable dataTable;
        if (dataFile == null) {
            /*execute*/
            dataTable = null;

            dataFile = new DataFile(DataFileType.IN_MARKDOWN, "/", ProjectUtil.newElementId(project), ProjectUtil.newElementId(project));
            dataFile.setId(ProjectUtil.newUniqueId(project));

        } else {
            /*executeUndo*/
            dataTable = (DataTable) paramMap.get(CommandParamKey.DATA_TABLE);
            dataFile.setOwner(dataTable);
        }
        List<DataFile> fileList = step.getFileList();
        fileList.add(dataFile);

        Floor floor = tower.getAvailableFloor(1, false);
        floor.setRoom(1, dataFile);

        String selectableId = dataFile.getSelectableId();
        step.getSelectableMap().put(selectableId, dataFile);

        /*line between dataFile and dataTable*/
        Line newLine = null;
        if (dataTable != null) {
            newLine = addLine(selectableId, dataTable.getSelectableId());
            newLine.setId(ProjectUtil.newUniqueId(project));
        }

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.DATA_FILE, dataFile);

        /*Action Result*/
        action.getResultMap().put(ActionResultKey.DATA_FILE, dataFile);

        // save DataFile list
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.DATA_FILE_LIST, mapper.fromDataFileList(fileList), projectUser, 0, stepId);

        // save DataFile data
        dataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), projectUser, dataFile.getId(), stepId);

        if (newLine != null) {
            // save Line data
            dataManager.addData(ProjectFileType.LINE, mapper.map(newLine), projectUser, newLine.getId(), stepId);

            // save Line list
            dataManager.addData(ProjectFileType.LINE_LIST, mapper.fromLineList(step.getLineList()), projectUser, newLine.getId(), stepId);

            // no object at the startPlug to save here
        }

        // save Tower data
        dataManager.addData(ProjectFileType.TOWER, mapper.map(tower), projectUser, tower.getId(), stepId);

        // save Step data: need to update Step record every Line added*/
        dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());
    }

}
