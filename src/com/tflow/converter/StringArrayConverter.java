package com.tflow.converter;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "StringArray")
public class StringArrayConverter implements Converter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        String separator = getSeparator(context, component);

        String[] strings = (String[]) object;
        StringBuilder joined = new StringBuilder();
        for (String string : strings) {
            joined.append(separator).append(string);
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