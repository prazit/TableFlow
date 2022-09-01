package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.view.PropertyView;
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

        boolean hasEvent = selectable instanceof HasEvent;
        LoggerFactory.getLogger(ChangePropertyValue.class).debug("{} hasEvent = {} ", selectable.getClass().getName(), hasEvent);
        if (hasEvent) {
            EventManager eventManager = ((HasEvent) selectable).getEventManager();
            eventManager.fireEvent(EventName.PROPERTY_CHANGED, property);
        }

        // for Action.executeUndo
        /*paramMap.put(CommandParamKey.PROPERTY, property);*/
        Object oldValue = property.getOldValue();
        property.setOldValue(property.getNewValue());
        property.setNewValue(oldValue);

        // result map

        // Specific: ColumnFx.function is changed
        if (selectable instanceof ColumnFx && PropertyVar.function.equals(property.getVar())) {
            createEndPlugList((ColumnFx) selectable);
        }

        // save data
        if (!saveSelectableData(selectable, step)) {
            throw new UnsupportedOperationException("Change Property Value of Unknown Object Type " + selectable.getClass().getName() + ", property=" + property);
        }

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
                log.warn("ChangePropertyValue.setPropertyValue(oldValue:{}): using method {}({}:{})", property.getOldValue(), method.getName(), toCSVString(parameterTypes), value);
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
