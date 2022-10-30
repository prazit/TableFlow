package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class LoadStep extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        int stepIndex = (Integer) paramMap.get(CommandParamKey.INDEX);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        Logger log = LoggerFactory.getLogger(LoadStep.class);
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

        /*regenerate selectableMap*/
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        selectableMap.clear();
        selectableMap.put(project.getSelectableId(), project);
        if (isSelectProject) {
            manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getVariableMap().values()));
            manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getDatabaseMap().values()));
            manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getSftpMap().values()));
            manager.collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getLocalMap().values()));
        } else {
            selectableMap.put(step.getSelectableId(), step);
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getDataTower().getFloorList()));
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getTransformTower().getFloorList()));
            manager.collectSelectableTo(selectableMap, manager.getSelectableList(step.getOutputTower().getFloorList()));
        }

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

        // result map
        action.getResultMap().put(ActionResultKey.STEP, step);

        // no data to save

    }

}
