package com.tflow.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@FacesConverter(value = "StringArray")
public class StringArrayConverter implements Converter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        String separator = getSeparator(context, component);

        String[] strings = (String[]) object;
        String joined = "";
        for (String string : strings) {
            joined += separator + string;
        }
        return joined.substring(1);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String string) {
        String separator = getSeparator(context, component);
        return string.split("[" + separator + "]");
    }

    private String getSeparator(FacesContext context, UIComponent component) {
        ValueExpression valueEx = component.getValueExpression("converterParam");
        Object value = (valueEx == null) ? "\n" : valueEx.getValue(context.getELContext());
        return (value == null) ? "\n" : value.toString();
    }
}