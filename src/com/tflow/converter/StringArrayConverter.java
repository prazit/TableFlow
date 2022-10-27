package com.tflow.converter;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.ArrayList;
import java.util.Arrays;

@FacesConverter(value = "StringArray")
public class StringArrayConverter implements Converter {

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        String separator = getSeparator(context, component);

        String[] strings;
        if (object instanceof ArrayList) {
            ArrayList<Object> arrayList = (ArrayList<Object>) object;
            strings = Arrays.copyOf(arrayList.toArray(), arrayList.size(), String[].class);
        } else {
            strings = (String[]) object;
        }

        StringBuilder joined = new StringBuilder();
        for (String string : strings) {
            joined.append(separator).append(string);
        }
        return joined.length() < 1 ? "" : joined.substring(1);
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