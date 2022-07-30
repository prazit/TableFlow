package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataException;
import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
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

        List<Step> stepList = project.getStepList();
        int size = stepList.size();
        if (stepIndex < 0) {
            stepIndex = 0;
        } else if (stepIndex >= size) {
            stepIndex = size - 1;
        }

        Step step = stepList.get(stepIndex);
        if (step == null) {
            throw new UnsupportedOperationException("SelectStep with invalid index(" + stepIndex + "), Project(" + project.getSelectableId() + ") has " + size + " step(s)");
        }

        ProjectDataManager dataManager = project.getDataManager();
        boolean loadStepData = step.getIndex() < 0;
        if (loadStepData) {
            log.warn("selectStep({}): load step data...", stepIndex);
            try {
                step = dataManager.getStep(project, stepIndex);
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
        selectableMap.put(step.getSelectableId(), step);
        collectSelectableTo(selectableMap, getSelectableList(step.getDataTower().getFloorList()));
        collectSelectableTo(selectableMap, getSelectableList(step.getTransformTower().getFloorList()));
        collectSelectableTo(selectableMap, getSelectableList(step.getOutputTower().getFloorList()));
        collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getDatabaseMap().values()));
        collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getSftpMap().values()));
        collectSelectableTo(selectableMap, new ArrayList<Selectable>(project.getLocalMap().values()));
        log.warn("SelectStep: after generate selectableMap step={}", step);

        // need activeObject by selectableId.
        if (loadStepData) {
            Selectable activeObject = selectableMap.get(step.getActiveObject().getSelectableId());
            step.setActiveObject(activeObject == null ? step : activeObject);
        }

        // need to correct line index, need real plug by selectableId.
        int clientIndex = 0;
        for (Line line : step.getLineList()) {
            line.setClientIndex(clientIndex++);
            if (loadStepData) {
                log.warn("line={}", line);
                try {
                    line.setStartPlug(selectableMap.get(line.getStartSelectableId()).getStartPlug());
                } catch (NullPointerException ex) {
                    log.error("startSelectableId:{} not found", line.getStartSelectableId());
                }

                try {
                    line.setEndPlug(((HasEndPlug) selectableMap.get(line.getEndSelectableId())).getEndPlug());
                } catch (NullPointerException ex) {
                    log.error("endSelectableId:{} not found", line.getEndSelectableId());
                }
            }
        }
        step.setLastLineClientIndex(clientIndex);

        // for Action.executeUndo
        paramMap.put(CommandParamKey.INDEX, oldStepIndex);

        // result map
        action.getResultMap().put(ActionResultKey.STEP, step);

        // save Step data
        int stepId = step.getId();
        dataManager.addData(ProjectFileType.STEP, dataManager.mapper.map(step), project, stepId, stepId);

        // save Project data
        dataManager.addData(ProjectFileType.PROJECT, dataManager.mapper.map(project), project, project.getId());

    }

    /**
     * IMPORTANT: when selectable-object-type is added, need to add script to collect them when Select step as Active-Step.
     */
    private void collectSelectableTo(Map<String, Selectable> map, List<Selectable> selectableList) {
        for (Selectable selectable : selectableList) {
            map.put(selectable.getSelectableId(), selectable);
            if (selectable instanceof DataTable) {
                DataTable dt = (DataTable) selectable;

                for (DataColumn column : dt.getColumnList()) {
                    map.put(column.getSelectableId(), column);
                }

                for (DataFile output : dt.getOutputList()) {
                    map.put(output.getSelectableId(), output);
                }

                if (selectable instanceof TransformTable) {
                    TransformTable tt = (TransformTable) selectable;
                    for (ColumnFx columnFx : tt.getColumnFxTable().getColumnFxList()) {
                        map.put(columnFx.getSelectableId(), columnFx);

                        for (ColumnFxPlug columnFxPlug : columnFx.getEndPlugList()) {
                            map.put(columnFxPlug.getSelectableId(), columnFxPlug);
                        }
                    }

                    for (TableFx tableFx : tt.getFxList()) {
                        map.put(tableFx.getSelectableId(), tableFx);
                    }
                }

            }
        }
    }

    public List<Selectable> getSelectableList(List<Floor> floorList) {
        List<Selectable> selectableList = new ArrayList<>();
        for (Floor floor : floorList) {
            selectableList.addAll(collectSelectableRoom(floor.getRoomList()));
        }
        return selectableList;
    }

    public List<Selectable> collectSelectableRoom(List<Room> roomList) {
        List<Selectable> selectableList = new ArrayList<>();
        for (Room room : roomList) {
            if (room instanceof Selectable) {
                selectableList.add((Selectable) room);
            }
        }
        return selectableList;
    }

}
