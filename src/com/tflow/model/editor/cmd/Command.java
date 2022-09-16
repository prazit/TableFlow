package com.tflow.model.editor.cmd;

import com.tflow.model.data.DataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.Package;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

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

    /**
     * Notice: IMPORTANT: when ProjectFileType is added, need to add script here to save data of them on event PropertyChanged.
     */
    protected boolean saveSelectableData(ProjectFileType projectFileType, Object dataObject, DataManager dataManager, ProjectUser projectUser) {
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        Step step;
        int stepId;
        switch (projectFileType) {
            case PROJECT:
                dataManager.addData(ProjectFileType.PROJECT, mapper.map((Project) dataObject), projectUser, ((Project) dataObject).getId());
                break;
            case STEP:
                step = (Step) dataObject;
                stepId = step.getId();
                dataManager.addData(ProjectFileType.STEP, mapper.map(step), projectUser, stepId, stepId);
                break;
            case DATA_COLUMN:
                DataColumn dataColumn = (DataColumn) dataObject;
                step = dataColumn.getOwner().getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map(dataColumn), projectUser, dataColumn.getId(), stepId, dataColumn.getOwner().getId());
                break;
            case TRANSFORM_COLUMN:
                TransformColumn transformColumn = (TransformColumn) dataObject;
                step = transformColumn.getOwner().getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.TRANSFORM_COLUMN, mapper.map(transformColumn), projectUser, transformColumn.getId(), stepId, 0, transformColumn.getOwner().getId());
                break;
            case STEP_LIST:
                dataManager.addData(ProjectFileType.STEP_LIST, mapper.fromStepList((List<Step>) dataObject), projectUser, "");
                break;
            case DB:
                dataManager.addData(ProjectFileType.DB, mapper.map((Database) dataObject), projectUser, ((Database) dataObject).getId());
                break;
            case SFTP:
                dataManager.addData(ProjectFileType.SFTP, mapper.map((SFTP) dataObject), projectUser, ((SFTP) dataObject).getId());
                break;
            case LOCAL:
                dataManager.addData(ProjectFileType.LOCAL, mapper.map((Local) dataObject), projectUser, ((Local) dataObject).getId());
                break;
            case DATA_SOURCE_SELECTOR:
                DataSourceSelector dataSourceSelector = (DataSourceSelector) dataObject;
                step = dataSourceSelector.getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map(dataSourceSelector), projectUser, dataSourceSelector.getId(), stepId);
                break;
            case DATA_FILE:
                DataFile dataFile = (DataFile) dataObject;
                step = ((DataTable) dataFile.getOwner()).getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.DATA_FILE, mapper.map(dataFile), projectUser, dataFile.getId(), stepId);
                break;
            case DATA_TABLE:
                DataTable dataTable = (DataTable) dataObject;
                step = dataTable.getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map(dataTable), projectUser, dataTable.getId(), stepId, dataTable.getId());
                break;
            case TRANSFORM_TABLE:
                TransformTable transformTable = (TransformTable) dataObject;
                step = transformTable.getOwner();
                stepId = step.getId();
                dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map(transformTable), projectUser, transformTable.getId(), stepId, 0, transformTable.getId());
                break;

            case GROUP_LIST:
                dataManager.addData(ProjectFileType.GROUP_LIST, mapper.map((ProjectGroupList) dataObject), projectUser, 0);
                break;
            case GROUP:
                dataManager.addData(ProjectFileType.GROUP, mapper.map((ProjectGroup) dataObject), projectUser, ((ProjectGroup) dataObject).getId());
                break;

            case PACKAGE_LIST:
                dataManager.addData(ProjectFileType.PACKAGE_LIST, mapper.fromPackageList((List<Item>) dataObject), projectUser, "");
                break;
            case PACKAGE:
                dataManager.addData(ProjectFileType.PACKAGE, mapper.map((Package) dataObject), projectUser, ((Package) dataObject).getId());
                break;

            /*Notice: need to find Changeable List and do the same way of STEP_LIST
             * Changeable List are ItemData familiar.
             * 1. [X] Project.stepList (Item) | [X] Step.eventManager.addHandler(Event.NAME_CHANGED) | [X] EditorController.createStepEventHandlers | [X] TEST
             * 2. [X] GroupList (GroupItem) | [X] Group.eventManager.addHandler(Event.NAME_CHANGED) | [X] GroupController.createEventHandlers | [X] TEST
             *    [X] Group.xhtml: need to make editable group-name look like setting in KUDU-App (double click then edit and click save).
             * 3. [X] Group.ProjectList (ProjectItem) | [X] Project.eventManager.addHandler(Event.NAME_CHANGED) | [X] FlowchartController.createEventHandlers + ProjectController.createEventHandlers | [X] TEST
             * 4. [X] PackageList (Item) | [X] Package.eventManager.addHandler(Event.NAME_CHANGED) | [X] ProjectController.createEventHandlers | [X] TEST
             **/
            /*case CLIENT_LIST:
                break;
            case CLIENT:
                break;
            case VERSIONED_LIST:
                break;
            case VERSIONED:
                break;
            case UPLOADED_LIST:
                break;
            case UPLOADED:
                break;
            case GENERATED_LIST:
                break;
            case GENERATED:
                break;
            case DB_LIST:
                break;
            case SFTP_LIST:
                break;
            case LOCAL_LIST:
                break;
            case DS_LIST:
                break;
            case DS:
                break;
            case VARIABLE_LIST:
                break;
            case VARIABLE:
                break;
            case DATA_SOURCE_SELECTOR_LIST:
                break;
            case DATA_FILE_LIST:
                break;
            case DATA_TABLE_LIST:
                break;
            case DATA_COLUMN_LIST:
                break;
            case DATA_OUTPUT_LIST:
                break;
            case DATA_OUTPUT:
                break;
            case TRANSFORM_TABLE_LIST:
                break;
            case TRANSFORM_COLUMN_LIST:
                break;
            case TRANSFORMATION_LIST:
                break;
            case TRANSFORM_OUTPUT_LIST:
                break;
            case TRANSFORM_COLUMNFX:
                break;
            case TRANSFORMATION:
                break;
            case TRANSFORM_OUTPUT:
                break;
            case TOWER:
                break;
            case FLOOR:
                break;
            case LINE_LIST:
                break;
            case LINE:
                break;*/

            default:
                return false;
        }

        return true;
    }

}
