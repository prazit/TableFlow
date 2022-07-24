package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSourceSelector;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ChangePropertyValue extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Selectable selectable = (Selectable) paramMap.get(CommandParamKey.SELECTABLE);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);

        try {
            setPropertyValue(selectable, property);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Cannot set property(" + property + ") to selectable(" + selectable.getSelectableId() + ")", ex);
        }

        // for Action.executeUndo
        Object oldValue = property.getOldValue();
        property.setOldValue(property.getNewValue());
        property.setNewValue(oldValue);
        /*paramMap.put(CommandParamKey.PROPERTY, property);*/

        // result

        // save data
        int stepId = step.getId();
        Project project = step.getOwner();
        ProjectDataManager dataManager = project.getDataManager();
        ProjectMapper mapper = dataManager.mapper;
        if (selectable instanceof DataColumn) dataManager.addData(ProjectFileType.DATA_COLUMN, mapper.map((DataColumn) selectable), project, ((DataColumn) selectable).getId(), step.getId(), ((DataColumn) selectable).getOwner().getId());
        else if (selectable instanceof ColumnFx) dataManager.addData(ProjectFileType.TRANSFORM_COLUMNFX, mapper.map((ColumnFx) selectable), project, ((ColumnFx) selectable).getId(), step.getId(), 0, ((ColumnFx) selectable).getOwner().getOwner().getId());
        else if (selectable instanceof DataFile) dataManager.addData(ProjectFileType.DATA_FILE, mapper.map((DataFile) selectable), project, ((DataFile) selectable).getId(), stepId);
        else if (selectable instanceof DataSourceSelector) dataManager.addData(ProjectFileType.DATA_SOURCE_SELECTOR, mapper.map((DataSourceSelector) selectable), project, ((DataSourceSelector) selectable).getId(), stepId);
        else if (selectable instanceof TransformTable) dataManager.addData(ProjectFileType.TRANSFORM_TABLE, mapper.map((TransformTable) selectable), project, ((TransformTable) selectable).getId(), stepId, 0, ((TransformTable) selectable).getId());
        else if (selectable instanceof DataTable) dataManager.addData(ProjectFileType.DATA_TABLE, mapper.map((DataTable) selectable), project, ((DataTable) selectable).getId(), stepId, ((DataTable) selectable).getId());
        else if (selectable instanceof Step) dataManager.addData(ProjectFileType.STEP, mapper.map((Step) selectable), project, ((Step) selectable).getId(), stepId);
        else if (selectable instanceof Project) dataManager.addData(ProjectFileType.PROJECT, mapper.map((Project) selectable), project, ((Project) selectable).getId());
        else throw new UnsupportedOperationException("Change Property Value of Unknown Object Type " + selectable.getClass().getName() + ", property=" + property);
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
                Class<?>[] parameterTypes = method.getParameterTypes();
                log.warn("ChangePropertyValue.setPropertyValue(oldValue:{}): using method {}({}:{})", property.getOldValue(), method.getName(), toCSVString(parameterTypes), value);
                method.invoke(selectable, parameterTypes[0].cast(value));
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
