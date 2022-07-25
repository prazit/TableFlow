package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Command {

    private Step step;

    public void setStep(Step step) {
        this.step = step;
    }

    public abstract void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException;

    protected int newLineClientIndex() {
        int lineClientIndex = step.getLastLineClientIndex() + 1;
        step.setLastLineClientIndex(lineClientIndex);
        return lineClientIndex;
    }

    /**
     * Internal use to add new line to the step without history.
     */
    protected Line addLine(String startSelectableId, String endSelectableId) {
        List<Line> lineList = step.getLineList();
        Map<String, Selectable> selectableMap = step.getSelectableMap();

        Line newLine = new Line(startSelectableId, endSelectableId);

        newLine.setClientIndex(newLineClientIndex());
        lineList.add(newLine);

        Selectable startSelectable = selectableMap.get(startSelectableId);
        Selectable endSelectable = selectableMap.get(endSelectableId);

        newLine.setType(getLineType(startSelectable));

        LinePlug startPlug = startSelectable.getStartPlug();
        startPlug.setPlugged(true);
        startPlug.getLineList().add(newLine);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setStartPlug(startPlug);

        HasEndPlug hasEndPlug = (HasEndPlug) endSelectable;
        LinePlug endPlug = hasEndPlug.getEndPlug();
        endPlug.setPlugged(true);
        endPlug.getLineList().add(newLine);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.plugged(newLine);
        }
        newLine.setEndPlug(endPlug);

        return newLine;
    }

    /**
     * Internal use to remove line from the step without history.
     */
    protected void removeLine(Line line) {
        if (line == null) return;
        step.getLineList().remove(line);

        LinePlug startPlug = line.getStartPlug();
        startPlug.getLineList().remove(line);
        PlugListener listener = startPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }

        LinePlug endPlug = line.getEndPlug();
        endPlug.getLineList().remove(line);
        listener = endPlug.getListener();
        if (listener != null) {
            listener.unplugged(line);
        }
    }

    /**
     * Internal use to remove line from the step without history.
     */
    protected void removeLine(LinePlug plug) {
        List<Line> lineList = new ArrayList<>(plug.getLineList());
        if (lineList.size() > 0) {
            for (Line line : lineList) {
                removeLine(line);
            }
        }
    }

    protected LineType getLineType(Selectable selectable) {
        if (selectable instanceof DataColumn) {
            DataColumn dataColumn = (DataColumn) selectable;
            return LineType.valueOf(dataColumn.getType().name());
        } else if (selectable instanceof ColumnFx) {
            ColumnFx columnFx = (ColumnFx) selectable;
            return LineType.valueOf(columnFx.getOwner().getType().name());
        } else {
            return LineType.TABLE;
        }
    }

    /**
     * Need to re-create endPlugList again after the function is changed.
     */
    protected void createEndPlugList(ColumnFx columnFx) {
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        List<ColumnFxPlug> endPlugList = columnFx.getEndPlugList();
        if (endPlugList.size() > 0) {
            /*need to remove old list from selectableMap before reset the list*/
            for (ColumnFxPlug columnFxPlug : endPlugList) {
                selectableMap.remove(columnFxPlug.getSelectableId());
                removeLine(columnFxPlug.getLine());
            }
            endPlugList.clear();
        }

        String endPlugId;
        for (PropertyView propertyView : columnFx.getFunction().getProperties().getPlugPropertyList()) {
            endPlugId = ProjectUtil.newElementId(project);
            /*Notice: columnFxPlug use defaultPlugListener*/
            ColumnFxPlug columnFxPlug = new ColumnFxPlug(ProjectUtil.newUniqueId(project), propertyView.getType().getDataType(), propertyView.getLabel(), endPlugId, columnFx);
            endPlugList.add(columnFxPlug);
            /*update selectableMap for each*/
            selectableMap.put(columnFxPlug.getSelectableId(), columnFxPlug);
        }
    }

    protected boolean saveSelectableData(Selectable selectable, Step step) {
        Project project = step.getOwner();
        ProjectDataManager dataManager = project.getDataManager();
        int stepId = step.getId();
        ProjectMapper mapper = dataManager.mapper;
        if (selectable instanceof DataColumn) dataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map((DataColumn) selectable), project, ((DataColumn) selectable).getId(), step.getId(), ((DataColumn) selectable).getOwner().getId());
        else if (selectable instanceof ColumnFx) dataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, mapper.map((ColumnFx) selectable), project, ((ColumnFx) selectable).getId(), step.getId(), 0, ((ColumnFx) selectable).getOwner().getOwner().getId());
        else if (selectable instanceof DataFile) dataManager.addData(ProjectFileType.DATA_FILE, mapper.map((DataFile) selectable), project, ((DataFile) selectable).getId(), stepId);
        else if (selectable instanceof DataSourceSelector) dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map((DataSourceSelector) selectable), project, ((DataSourceSelector) selectable).getId(), stepId);
        else if (selectable instanceof TransformTable) dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map((TransformTable) selectable), project, ((TransformTable) selectable).getId(), stepId, 0, ((TransformTable) selectable).getId());
        else if (selectable instanceof DataTable) dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map((DataTable) selectable), project, ((DataTable) selectable).getId(), stepId, ((DataTable) selectable).getId());
        else if (selectable instanceof Step) dataManager.addData(ProjectFileType.STEP, mapper.map((Step) selectable), project, ((Step) selectable).getId(), stepId);
        else if (selectable instanceof Project) dataManager.addData(ProjectFileType.PROJECT, mapper.map((Project) selectable), project, ((Project) selectable).getId());
        else return false;
        return true;
    }

}
