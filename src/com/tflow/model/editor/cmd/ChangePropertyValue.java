package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ChangePropertyValue extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        ProjectFileType projectFileType = (ProjectFileType) paramMap.get(CommandParamKey.PROJECT_FILE_TYPE);
        Object dataObject = paramMap.get(CommandParamKey.DATA);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);

        Selectable selectable = null;
        if (dataObject instanceof Selectable) {
            selectable = (Selectable) dataObject;
            try {
                setPropertyValue(selectable, property);
            } catch (Exception ex) {
                throw new UnsupportedOperationException("Cannot set property(" + property + ") to selectable(" + selectable.getSelectableId() + ")", ex);
            }
        }

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
        if (!saveSelectableData(projectFileType, dataObject, dataManager, projectUser)) {
            throw new UnsupportedOperationException("Change Property Value of Unsupported type=" + projectFileType + " dataObject=" + dataObject.getClass().getName() + ", property=" + property);
        }

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
        
    }

    private String propertyToMethod(String propertyName) {
        return "set" +
                propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
    }

    private void setPropertyValue(Selectable selectable, PropertyView property) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassCastException {
        Object value = property.getNewValue();
        Map<String, Object> propertyMap = selectable.getPropertyMap();
        String propertyName = property.getVar();
        if (propertyMap != null && propertyMap.containsKey(propertyName)) {
            propertyMap.put(propertyName, value);
            return;
        }

        /*TODO: remove log*/
        Logger log = LoggerFactory.getLogger(ChangePropertyValue.class);
        //log.warn("setPropertyValue(selectable:{}, property:{})", selectable.getSelectableId(), property);

        /*by setValue() method*/
        String methodName = propertyToMethod(propertyName);
        Method[] methods = selectable.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().compareTo(methodName) == 0) {
                Class[] parameterTypes = method.getParameterTypes();
                Class parameterClass = parameterTypes[0];
                if (log.isDebugEnabled()) log.debug("ChangePropertyValue.setPropertyValue(oldValue:{}): using method {}({}:{})", property.getOldValue(), method.getName(), toCSVString(parameterTypes), value);
                if (value == null) {
                    method.invoke(selectable, parameterClass.cast(null));
                } else if (value instanceof Integer || value instanceof Long) {
                    method.invoke(selectable, value);
                } else if (parameterClass.isEnum() && value instanceof String) {
                    if (((String) value).isEmpty()) {
                        method.invoke(selectable, parameterClass.cast(null));
                    } else {
                        method.invoke(selectable, Enum.valueOf(parameterClass, (String) value));
                    }
                } else if (!parameterClass.isInstance(value)) {
                    method.invoke(selectable, parameterClass.cast(value));
                } else {
                    method.invoke(selectable, value);
                }

                return;
            }
        }

        throw new NoSuchMethodException("No method " + methodName + "(Object) in " + selectable.getClass().getName() + "[" + selectable.getSelectableId() + "]");
    }

    private String toCSVString(Class<?>[] parameterTypes) {
        if (parameterTypes.length == 0) return "";

        StringBuilder stringBuilder = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            stringBuilder.append(parameterType.getName()).append(",");
        }
        return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
    }

}
