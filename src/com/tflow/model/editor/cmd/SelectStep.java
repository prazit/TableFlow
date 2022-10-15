package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SelectStep extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        int stepIndex = (Integer) paramMap.get(CommandParamKey.INDEX);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Logger log = LoggerFactory.getLogger(SelectStep.class);
        int oldStepIndex = project.getActiveStepIndex();
        boolean isSelectProject = stepIndex < 0;

        /*move stepIndex into the list*/
        StepList<Step> stepList = project.getStepList();
        int size = stepList.size();
        if (stepIndex >= size) {
            stepIndex = size - 1;
        }
        Step step = stepList.get(stepIndex);

        ProjectManager manager = project.getManager();
        boolean loadStepData = !isSelectProject && step.getIndex() < 0;
        if (loadStepData) {
            log.warn("selectStep({}): load step data...", stepIndex);
            try {
                step = manager.loadStep(project, stepIndex);
                log.info("selectStep: loaded step = {}", step);
            } catch (ProjectDataException ex) {
                throw new UnsupportedOperationException("SelectStep found error reported from TRcmd service: ", ex);
            } catch (Exception ex) {
                throw new UnsupportedOperationException("SelectStep found unexpected error, ", ex);
            }
            stepList.remove(stepIndex);
            stepList.add(stepIndex, step);
            step.setIndex(stepIndex);
        }

        project.setActiveStepIndex(stepIndex);

        /*regenerate selectableMap*/
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        selectableMap.clear();
        selectableMap.put(project.getSelectableId(), project);
        if (!isSelectProject) {
            selectableMap.put(step.getSelectableId(), step);
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getDataTower().getFloorList()));
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getTransformTower().getFloorList()));
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getOutputTower().getFloorList()));
        }
        manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getDatabaseMap().values()));
        manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getSftpMap().values()));
        manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getLocalMap().values()));

        // need activeObject by selectableId.
        if (loadStepData) {
            Selectable activeObject = selectableMap.get(step.getActiveObject().getSelectableId());
            step.setActiveObject(activeObject == null ? step : activeObject);
        }

        // need to correct line index, need real plug by selectableId.
        int clientIndex = 0;
        if (!isSelectProject) for (Line line : step.getLineList()) {
            line.setClientIndex(clientIndex++);
            if (loadStepData) {
                log.debug("line={}", line);
                try {
                    LinePlug startPlug = selectableMap.get(line.getStartSelectableId()).getStartPlug();
                    line.setStartPlug(startPlug);
                    startPlug.getLineList().add(line);
                } catch (NullPointerException ex) {
                    log.error("startSelectableId:{} not found", line.getStartSelectableId());
                    log.trace("", ex);
                }

                try {
                    LinePlug endPlug = ((HasEndPlug) selectableMap.get(line.getEndSelectableId())).getEndPlug();
                    line.setEndPlug(endPlug);
                    endPlug.getLineList().add(line);
                } catch (NullPointerException ex) {
                    log.error("endSelectableId:{} not found", line.getEndSelectableId());
                    log.trace("", ex);
                }
            }
        }
        step.setLastLineClientIndex(clientIndex);

        // for Action.executeUndo
        paramMap.put(CommandParamKey.INDEX, oldStepIndex);

        // result map
        action.getResultMap().put(ActionResultKey.STEP, step);

        // save Step data
        DataManager dataManager = project.getDataManager();
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        ProjectUser projectUser = mapper.toProjectUser(project);
        if (!isSelectProject) {
            int stepId = step.getId();
            dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);
        }

        // save Project data
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }

}
