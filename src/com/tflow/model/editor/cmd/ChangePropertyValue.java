package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.view.PropertyView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ChangePropertyValue extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Logger log = LoggerFactory.getLogger(getClass());

        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        ProjectFileType projectFileType = (ProjectFileType) paramMap.get(CommandParamKey.PROJECT_FILE_TYPE);
        Object dataObject = paramMap.get(CommandParamKey.DATA);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);

        /*TODO: try to remove set value within the ChangePropertyValue command
                or use some parameters to switch on/off to support Undo/Redo command.*/
        /*if (dataObject instanceof Selectable) {
            Selectable selectable = (Selectable) dataObject;
            try {
                selectable.getProperties().setPropertyValue(selectable, property, log);
            } catch (Exception ex) {
                throw new UnsupportedOperationException("Cannot set property(" + property + ") to selectable(" + selectable.getSelectableId() + ")", ex);
            }
        }*/

        boolean hasEvent = dataObject instanceof HasEvent;
        LoggerFactory.getLogger(ChangePropertyValue.class).debug("{} hasEvent = {} ", dataObject.getClass().getName(), hasEvent);
        if (hasEvent) {
            EventManager eventManager = ((HasEvent) dataObject).getEventManager();
            eventManager.fireEvent(EventName.PROPERTY_CHANGED, property);
        }

        // for Action.executeUndo
        /*paramMap.put(CommandParamKey.PROPERTY, property);*/
        Object oldValue = property.getOldValue();
        property.setOldValue(property.getNewValue());
        property.setNewValue(oldValue);

        // result map

        // Specific: ColumnFx.function is changed
        if (dataObject instanceof ColumnFx && PropertyVar.function.equals(property.getVar())) {
            createEndPlugList((ColumnFx) dataObject);
        }

        // save data
        ProjectUser projectUser = workspace.getProjectUser();
        DataManager dataManager = workspace.getDataManager();
        Step step = workspace.getProject().getActiveStep();
        if (!saveSelectableData(projectFileType, dataObject, step, dataManager, projectUser)) {
            throw new UnsupportedOperationException("Change Property Value of Unsupported type=" + projectFileType + " dataObject=" + dataObject.getClass().getName() + ", property=" + property);
        }

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }
}
